package greencity.service;

import greencity.dto.event.*;
import greencity.dto.user.UserProfilePictureDto;
import greencity.entity.*;
import greencity.enums.Role;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.EventMappingContext;
import greencity.repository.*;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {
    private final EventRepo eventRepo;
    private final ModelMapper modelMapper;
    private final InitiativeTypeRepo initiativeTypeRepo;
    private final ImageRepo imageRepo;
    private final UserRepo userRepo;
    private final EventDateInfoRepo eventDateInfoRepo;
    private final EmailService emailService;
    private final ParticipationRepo participationRepo;
    private final EventDateInfoService eventDateInfoService;
    private final EventLikesRepo eventLikesRepo;

    private <T extends EventDateInfoDto> void validateEventRequest(List<T> eventDays) {
        if (eventDays == null || eventDays.isEmpty()) {
            throw new IllegalArgumentException("Event must have at least one event day.");
        }
        for (T e : eventDays) {
            if (e.getIsOnline() && e.getUrl() == null) {
                throw new IllegalArgumentException("If event is online it has to have the url");
            }
            if (e.getIsPlace() && e.getLocation() == null) {
                throw new IllegalArgumentException("If event is offline it has to have the location");
            }
        }
    }

    private Image handleImages(List<ImageRequestDto> imagesDto, ImageRequestDto mainImageDto) {
        Set<Image> images = new HashSet<>();
        Image mainImage = null;

        if (imagesDto == null || imagesDto.isEmpty()) {
            Image defaultImage = imageRepo.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Default image not found"));
            images.add(defaultImage);
            mainImage = defaultImage;
        } else {
            for (ImageRequestDto imageRequestDto : imagesDto) {
                Image image = saveImage(imageRequestDto.getImagePath());
                images.add(image);
            }
            if (mainImageDto != null) {
                mainImage = saveImage(mainImageDto.getImagePath());
            }

            if (mainImage == null) {
                mainImage = imageRepo.findByImagePath(imagesDto.get(0).getImagePath()).orElse(null);
            }
        }
        return mainImage;
    }

    private Event createEventFromRequest(EventRequestDto eventRequestDto, User author, Image mainImage,
        Set<Image> images) {
        Event event = modelMapper.map(eventRequestDto, Event.class);
        event.setCreationDate(ZonedDateTime.now());
        event.setAuthor(author);
        event.setImages(images);
        event.setMainImage(mainImage);
        return event;
    }

    private void saveEventDateInfo(EventRequestDto eventRequestDto, Event savedEvent) {
        List<EventDateInfoRequestDto> dtos = eventRequestDto.getEventDays().stream()
            .sorted(Comparator.comparing(EventDateInfoRequestDto::getEventTimeStart))
            .toList();

        int count = 1;

        for (EventDateInfoRequestDto infoRequestDto : dtos) {
            EventDateInfo eventDateInfo = modelMapper.map(infoRequestDto, EventDateInfo.class);
            eventDateInfo.setEvent(savedEvent);
            eventDateInfo.setNumOfDayInEvent(count);
            eventDateInfoRepo.save(eventDateInfo);

            count++;
        }
    }

    private void sendEventCreationEmail(User author, Event event) {
        String emailBody = String.format(
            "Dear %s,<br><br>Your event \"%s\" has been created.<br><br>Best regards,<br>Green City team",
            author.getName(), event.getTitle());
        String emailSubject = "\uD83D\uDD14 Your Event Creation Status";

        try {
            emailService.sendEmail(author.getEmail(), emailSubject, emailBody);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send event creation email", e);
        }
    }

    public Set<Image> createSetOfImages(List<ImageRequestDto> imagesDto) {

        Set<Image> setOfImages = (imagesDto == null || imagesDto.isEmpty())
            ? Set.of(Objects.requireNonNull(imageRepo.findById(1L).orElse(null)))
            : imagesDto.stream()
                .map(i -> modelMapper.map(i, Image.class))
                .map(image -> saveImage(image.getImagePath()))
                .collect(Collectors.toSet());

        return setOfImages;

    }

    @Override
    @Transactional
    public EventResponseDto createEvent(EventRequestDto eventRequestDto) {
        validateEventRequest(eventRequestDto.getEventDays());

        Event event = modelMapper.map(eventRequestDto, Event.class);
        User author = userRepo.findByEmail(eventRequestDto.getAuthorEmail()).orElse(null);

        Image mainImage = handleImages(eventRequestDto.getImages(), eventRequestDto.getMainImage());
        Set<Image> images = createSetOfImages(eventRequestDto.getImages());

        event.setImages(images);

        Event savedEvent = createEventFromRequest(eventRequestDto, author, mainImage, images);
        Event savedEventInRepo = eventRepo.save(savedEvent);

        saveEventDateInfo(eventRequestDto, savedEventInRepo);

        List<InitiativeType> initiativeTypes = eventRequestDto.getInitiativeTypes().stream()
            .map(i -> initiativeTypeRepo.findByName(i.getName())
                .orElseThrow(() -> new NotFoundException("Initiative type not found: " + i.getName())))
            .collect(Collectors.toList());
        savedEventInRepo.setInitiativeTypes(initiativeTypes);

        EventResponseDto eventResponseDto = modelMapper.map(savedEventInRepo, EventResponseDto.class);

        List<EventDateInfoResponseDto> eventDateInfoResponseDtos =
            eventDateInfoRepo.findByEvent(savedEventInRepo).stream()
                .map(e -> modelMapper.map(e, EventDateInfoResponseDto.class))
                .collect(Collectors.toList());

        eventResponseDto.setEventDays(eventDateInfoResponseDtos);

        assert author != null;
        sendEventCreationEmail(author, savedEventInRepo);

        return eventResponseDto;
    }

    private Image saveImage(String imagePath) {
        Optional<Image> existingImage = imageRepo.findByImagePath(imagePath);

        if (existingImage.isPresent()) {
            return existingImage.get();
        } else {
            Image newImage = Image.builder().imagePath(imagePath).build();
            return imageRepo.save(newImage);
        }
    }

    private boolean checkParticipation(Long userId, Long eventId) {
        List<User> participants = participationRepo.findUsersByEventId(eventId);
        return participants.stream().anyMatch(p -> p.getId().equals(userId));
    }

    @Override
    @Transactional
    public EventResponseDto updateEvent(Long id, EventUpdateDto eventUpdateDto, String email) {
        validateEventRequest(eventUpdateDto.getEventDays());
        validateUser(email, id);
        validateDate(id);

        Event existingEvent = eventRepo.findById(id).orElseThrow(() -> new NotFoundException("Event not found: " + id));

        existingEvent.setTitle(eventUpdateDto.getTitle());
        existingEvent.setDescription(eventUpdateDto.getDescription());

        Image mainImage = handleImages(eventUpdateDto.getImages(), eventUpdateDto.getMainImage());
        Set<Image> images = createSetOfImages(eventUpdateDto.getImages());

        existingEvent.setImages(images);
        existingEvent.setMainImage(mainImage);

        updateEventDateInfo(eventUpdateDto, id);

        List<InitiativeType> initiativeTypes = eventUpdateDto.getInitiativeTypes().stream()
            .map(i -> initiativeTypeRepo.findByName(i.getName())
                .orElseThrow(() -> new NotFoundException("Initiative type not found: " + i.getName())))
            .collect(Collectors.toList());
        existingEvent.setInitiativeTypes(initiativeTypes);

        List<EventDateInfoResponseDto> eventDays = eventDateInfoRepo.findByEvent(existingEvent).stream()
            .map(e -> modelMapper.map(e, EventDateInfoResponseDto.class))
            .sorted(Comparator.comparing(EventDateInfoResponseDto::getEventTimeStart))
            .toList();
        existingEvent.setDuration(eventDays.size());
        existingEvent.setOpen(eventUpdateDto.isOpen());
        EventResponseDto eventResponseDto = modelMapper.map(existingEvent, EventResponseDto.class);
        eventResponseDto.setEventDays(eventDays);
        eventResponseDto.setParticipants(participationRepo.findUsersByEventId(eventResponseDto.getId()).stream().map(
            p -> modelMapper.map(p, UserProfilePictureDto.class)).toList());
        eventResponseDto
            .setJoined(checkParticipation(userRepo.findByEmail(email).get().getId(), eventResponseDto.getId()));
        eventResponseDto.setLikes(eventLikesRepo.countLikesByEventId(existingEvent.getId()));

        return eventResponseDto;
    }

    @Transactional
    protected void updateEventDateInfo(EventUpdateDto eventUpdateDto, Long eventId) {
        Event existingEvent = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));

        List<EventDateInfoUpdateDto> eventDateInfoUpdateDtos = eventUpdateDto.getEventDays()
            .stream()
            .sorted(Comparator.comparing(EventDateInfoUpdateDto::getEventTimeStart))
            .toList();

        Set<Long> updatedIds = new HashSet<>();

        for (EventDateInfoUpdateDto infoUpdateDto : eventDateInfoUpdateDtos) {
            if (infoUpdateDto.getId() == null) {
                EventDateInfo eventDateInfo = modelMapper.map(infoUpdateDto, EventDateInfo.class);
                eventDateInfo.setEvent(existingEvent);
                eventDateInfoRepo.save(eventDateInfo);
                updatedIds.add(eventDateInfo.getId());
                infoUpdateDto.setId(eventDateInfo.getId());
            }
        }

        for (EventDateInfoUpdateDto eventDateInfoUpdateDto : eventDateInfoUpdateDtos) {
            EventDateInfo existingDateInfo = eventDateInfoRepo.findById(eventDateInfoUpdateDto.getId())
                .orElseThrow(() -> new NotFoundException("EventDateInfo not found"));
            if (eventDateInfoUpdateDto.getId() != null
                && (existingDateInfo.getEvent().getId().equals(eventId))) {
                eventDateInfoService.updateEventDateInfo(eventDateInfoUpdateDto.getId(), eventDateInfoUpdateDto);
            }
        }

        updatedIds.addAll(eventDateInfoUpdateDtos.stream()
            .map(EventDateInfoUpdateDto::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet()));

        List<EventDateInfo> eventDateInfos = eventDateInfoRepo.findByEvent(existingEvent);

        for (EventDateInfo eventDateInfo : eventDateInfos) {
            if (!updatedIds.contains(eventDateInfo.getId())) {
                eventDateInfoRepo.delete(eventDateInfo);
            }
        }

        List<EventDateInfo> eventDateInfosUpdated = eventDateInfoRepo.findByEvent(existingEvent);
        List<EventDateInfo> sortedEventDateInfos = eventDateInfosUpdated.stream()
            .sorted(Comparator.comparing(EventDateInfo::getEventTimeStart))
            .toList();

        int count1 = 1;

        for (EventDateInfo eventDateInfo : sortedEventDateInfos) {
            eventDateInfo.setNumOfDayInEvent(count1++);
        }
    }

    @Override
    public void deleteEvent(Long id) {

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventResponseDto> getEventById(Long id, String userEmail) {
        EventResponseDto eventResponseDto = eventRepo.findById(id).map(event -> modelMapper
            .map(event, EventResponseDto.class)).orElse(null);

        if (userRepo.findByEmail(userEmail).isPresent()) {
            assert eventResponseDto != null;
            eventResponseDto
                .setJoined(checkParticipation(userRepo.findByEmail(userEmail).get().getId(), eventResponseDto.getId()));
            eventResponseDto
                .setParticipants(participationRepo.findUsersByEventId(eventResponseDto.getId()).stream().map(
                    p -> modelMapper.map(p, UserProfilePictureDto.class)).toList());
            eventResponseDto.setEventDays(eventDateInfoRepo.findByEvent(eventRepo.findById(id).get())
                .stream().map(e -> modelMapper.map(e, EventDateInfoResponseDto.class))
                .sorted(Comparator.comparing(EventDateInfoResponseDto::getEventTimeStart))
                .toList());
        }
        if (eventResponseDto != null) {
            return Optional.of(eventResponseDto);
        } else {
            throw new NotFoundException("Event not found");
        }
    }

    @Override
    public EventProfilePreviewPageable getEventsByTitle(String title, Pageable pageable) {

//        if (title.length() < 1 || title.length() > 30 || !title.matches("[a-zA-Z0-9\\s.'-]+")) {
//            throw new IllegalArgumentException("Invalid title length or characters");
//        }

        Page<Event> events;
        if (title.isEmpty()) {
            events = eventRepo.findByTitleContainingIgnoreCaseSortedByDate(title, pageable);
        } else {
            events = eventRepo.findByTitleContainingIgnoreCaseSortedByTitle(title, pageable);
        }

        if (events.isEmpty()) {
            return new EventProfilePreviewPageable(
                Collections.emptyList(),
                0,
                0,
                0,
                0,
                true);
        }

        List<Event> listOfEvents = events.getContent();

        List<EventProfilePreviewDto> content = listOfEvents.stream()
            .map(event -> {
                EventDateInfo eventDateInfo = eventDateInfoRepo.findByEvent(event).stream()
                    .min(Comparator.comparing(EventDateInfo::getEventTimeStart))
                    .orElse(null);
                List<User> participants = participationRepo.findUsersByEventId(event.getId());
                EventMappingContext context = new EventMappingContext(event, eventDateInfo, participants);
                return modelMapper.map(context, EventProfilePreviewDto.class);
            })
            .toList();

        return new EventProfilePreviewPageable(
            content,
            events.getNumber(),
            events.getSize(),
            events.getTotalElements(),
            events.getTotalPages(),
            events.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents() {
        return eventRepo.findAll().stream()
            .map(event -> modelMapper.map(event, EventResponseDto.class))
            .collect(Collectors.toList());
    }

    @Override
    public EventProfilePreviewPageable getAllUserEvents(String userEmail, Pageable pageable) {
        return getUserEvents(userEmail, "ALL", pageable);
    }

    @Override
    public EventProfilePreviewPageable getAllUserPastEvents(String userEmail, Pageable pageable) {
        return getUserEvents(userEmail, "PAST", pageable);
    }

    @Override
    public EventProfilePreviewPageable getAllUserLiveEvents(String userEmail, Pageable pageable) {
        return getUserEvents(userEmail, "LIVE", pageable);
    }

    @Override
    public EventProfilePreviewPageable getAllUserUpcomingEvents(String userEmail, Pageable pageable) {
        return getUserEvents(userEmail, "UPCOMING", pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public EventProfilePreviewPageable getAllEventsPageable(Pageable pageable) {
        Page<Event> events = eventRepo.findAllSortedByStartDateAsc(pageable);
        List<Event> listOfEvents = events.getContent();

        List<EventProfilePreviewDto> content = listOfEvents.stream()
            .map(event -> {
                EventDateInfo eventDateInfo = eventDateInfoRepo.findByEvent(event).stream()
                    .min(Comparator.comparing(EventDateInfo::getEventDate)).orElse(null);
                List<User> participants = participationRepo.findUsersByEventId(event.getId());
                EventMappingContext context = new EventMappingContext(event, eventDateInfo, participants);
                return modelMapper.map(context, EventProfilePreviewDto.class);
            }).toList();

        return new EventProfilePreviewPageable(
            content,
            events.getNumber(),
            events.getSize(),
            events.getTotalElements(),
            events.getTotalPages(),
            events.isLast());
    }

    private EventProfilePreviewPageable getUserEvents(String userEmail, String type, Pageable pageable) {
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException("User not found: " + userEmail));

        LocalDateTime now = LocalDateTime.now();

        Page<Event> events;
        if ("ALL".equals(type)) {
            events = eventRepo.findAllByAuthorOrParticipant(user.getId(), pageable);
        } else {
            events = eventRepo.findUserEventsByTime(user.getId(), now, type, pageable);
        }

        List<Event> listOfEvents = events.getContent();

        List<EventProfilePreviewDto> content = listOfEvents.stream()
            .map(event -> {
                EventDateInfo eventDateInfo = eventDateInfoRepo.findByEvent(event).stream()
                    .min(Comparator.comparing(EventDateInfo::getEventDate)).orElse(null);
                List<User> participants = participationRepo.findUsersByEventId(event.getId());
                EventMappingContext context = new EventMappingContext(event, eventDateInfo, participants);
                return modelMapper.map(context, EventProfilePreviewDto.class);
            })
            .toList();

        return new EventProfilePreviewPageable(
            content,
            events.getNumber(),
            events.getSize(),
            events.getTotalElements(),
            events.getTotalPages(),
            events.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public EventProfilePreviewPageable getAllUserEventsByStatus(String userEmail, String status, Pageable pageable) {
        User user = userRepo.findByEmail(userEmail)
            .orElseThrow(() -> new NotFoundException("User not found: " + userEmail));

        boolean isOnline = "online".equalsIgnoreCase(status);

        Page<Event> events = eventRepo.findEventsByAuthorAndFirstDayOnlineStatus(user.getId(), isOnline, pageable);

        List<EventProfilePreviewDto> content = events.getContent().stream()
            .map(event -> {
                EventDateInfo eventDateInfo = eventDateInfoRepo.findByEvent(event).getFirst();
                List<User> participants = participationRepo.findUsersByEventId(event.getId());
                EventMappingContext context = new EventMappingContext(event, eventDateInfo, participants);
                return modelMapper.map(context, EventProfilePreviewDto.class);
            })
            .collect(Collectors.toList());

        return new EventProfilePreviewPageable(
            content,
            events.getNumber(),
            events.getSize(),
            events.getTotalElements(),
            events.getTotalPages(),
            events.isLast());
    }

    private void validateUser(String userEmail, Long id) {
        User user = userRepo.findByEmail(userEmail).orElse(null);
        Event event = eventRepo.findById(id).orElse(null);
        if (user == null) {
            throw new NotFoundException("User not found");
        } else if (!(Objects.equals(user.getId(), event.getAuthor().getId())
            || user.getRole().equals(Role.ROLE_ADMIN))) {
            throw new AccessDeniedException("You have no permission to update this event");
        }
    }

    private boolean validateDate(Long eventId) {
        Event event = eventRepo.findById(eventId).orElse(null);
        if (event == null) {
            throw new NotFoundException("Event not found");
        }

        List<EventDateInfo> eventDateInfos = eventDateInfoRepo.findByEvent(event);
        if (eventDateInfos.isEmpty()) {
            return false;
        }

        LocalDateTime latestEventDate = eventDateInfos.stream()
            .map(EventDateInfo::getEventTimeStart)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .orElse(null);

        if (latestEventDate != null && latestEventDate.isAfter(LocalDateTime.now())) {
            return true;
        }

        throw new BadRequestException("You cannot edit the event that is in the past");
    }
}
