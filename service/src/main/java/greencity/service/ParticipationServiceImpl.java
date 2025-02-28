package greencity.service;

import greencity.dto.event.EventResponseDto;
import greencity.dto.event.ParticipationRequestDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.entity.*;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.EventDateInfoRepo;
import greencity.repository.EventRepo;
import greencity.repository.ParticipationRepo;
import greencity.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ParticipationServiceImpl implements ParticipationService {
    private EventRepo eventRepo;
    private UserRepo userRepo;
    private ParticipationRepo participationRepo;
    private EventDateInfoRepo eventDateInfoRepo;
    private ModelMapper modelMapper;

    @Override
    public void addParticipation(ParticipationRequestDto participationRequestDto) {
        User user = userRepo.findById(participationRequestDto.getUserId())
            .orElseThrow(
                () -> new EntityNotFoundException("User not found with id: " + participationRequestDto.getUserId()));
        Event event = eventRepo.findById(participationRequestDto.getEventId())
            .orElseThrow(
                () -> new EntityNotFoundException("Event not found with id: " + participationRequestDto.getEventId()));

        Participation participation = new Participation(new ParticipationKey(user, event));
        participationRepo.save(participation);
    }

    @Override
    public void removeParticipation(Long userId, Long eventId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Event not found with id: " + eventId));

        List<EventDateInfo> dayInfos = eventDateInfoRepo.findByEvent(event);

        if (!dayInfos.isEmpty()) {
            Optional<LocalDateTime> eventDateLatest = eventDateInfoRepo.findByEvent(event).stream()
                .map(EventDateInfo::getEventTimeStart)
                .max(Comparator.naturalOrder());
            if (eventDateLatest.isPresent()) {
                if (eventDateLatest.get().isAfter(LocalDateTime.now())) {
                    ParticipationKey participationKey = new ParticipationKey(user, event);
                    Participation participation = participationRepo.findById(participationKey)
                        .orElseThrow(() -> new NotFoundException("Participation not found"));
                    participationRepo.delete(participation);
                } else {
                    throw new BadRequestException(
                        "You cannot remove the participation from the event that is in the past");
                }
            }
        }
    }

    @Override
    public List<UserProfilePictureDto> getUsersByEventId(Long eventId) {
        eventRepo.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        List<User> users = participationRepo.findUsersByEventId(eventId);

        return users.stream()
            .map(user -> modelMapper.map(user, UserProfilePictureDto.class)).toList();
    }

    @Override
    public List<EventResponseDto> getEventsByUserId(Long userId) {
        userRepo.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        List<Event> events = participationRepo.findEventsByUserId(userId);

        return events.stream().map(event -> modelMapper.map(event, EventResponseDto.class)).toList();
    }

    @Override
    public boolean isUserParticipating(Long userId, Long eventId) {
        if (!userRepo.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        if (!eventRepo.existsById(eventId)) {
            throw new EntityNotFoundException("Event not found with id: " + eventId);
        }

        List<Event> events = participationRepo.findEventsByUserId(userId);

        return events.stream().anyMatch(event -> event.getId() == eventId);
    }
}
