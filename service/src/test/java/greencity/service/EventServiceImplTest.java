package greencity.service;

import greencity.ModelUtils;
import greencity.dto.event.*;
import greencity.entity.*;
import greencity.enums.Role;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.EventMappingContext;
import greencity.repository.*;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepo eventRepo;

    @Mock
    private InitiativeTypeRepo initiativeTypeRepo;

    @Mock
    private EventDateInfoRepo eventDateInfoRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ImageRepo imageRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private EventLikesRepo eventLikesRepo;

    @Mock
    private EmailService emailService;

    @Mock
    private ParticipationRepo participationRepo;

    @Mock
    private EventDateInfoService eventDateInfoService; // Mocking the service

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private EventRequestDto eventRequestDto;
    private EventResponseDto eventResponseDto;
    private InitiativeType initiativeType;
    private EventDateInfoResponseDto eventDateInfoResponseDto;

    @BeforeEach
    void setUp() {
        eventRequestDto = new EventRequestDto();
        eventRequestDto.setTitle("Test Event");
        eventRequestDto.setDescription("This is a test event.");
        eventRequestDto.setEventDays(List.of(new EventDateInfoRequestDto(), new EventDateInfoRequestDto()));

        ImageRequestDto imageRequestDto = new ImageRequestDto();
        imageRequestDto.setImagePath("ImagePath");

        event = new Event();
        event.setId(1L);
        event.setTitle(eventRequestDto.getTitle());
        event.setDescription(eventRequestDto.getDescription());
        event.setCreationDate(ZonedDateTime.now());
        event.setImages(Set.of(new Image()));
        event.setAuthor(ModelUtils.getUser());

        eventResponseDto = new EventResponseDto();
        eventResponseDto.setId(event.getId());
        eventResponseDto.setTitle(event.getTitle());
        eventResponseDto.setDescription(event.getDescription());

        initiativeType = new InitiativeType();
        initiativeType.setId(1L);
        initiativeType.setName("Test Initiative Type");

        InitiativeTypeRequestDto initiativeTypeRequestDto = new InitiativeTypeRequestDto();
        initiativeTypeRequestDto.setName("Test Initiative Type");

        eventRequestDto.setInitiativeTypes(List.of(initiativeTypeRequestDto));
        eventRequestDto.setImages(List.of(imageRequestDto));
        eventRequestDto.setAuthorEmail(ModelUtils.getUser().getEmail());

        EventDateInfoRequestDto eventDateInfoRequestDto = new EventDateInfoRequestDto();
        eventDateInfoRequestDto.setIsOnline(true);
        eventDateInfoRequestDto.setUrl("http://google.com");
        eventDateInfoRequestDto.setIsPlace(false);

        eventRequestDto.setEventDays(List.of(eventDateInfoRequestDto));
    }

    @Test
    void createEvent_ShouldSaveAndReturnEventResponseDto() throws MessagingException {
        when(modelMapper.map(eventRequestDto, Event.class)).thenReturn(event);
        when(initiativeTypeRepo.findByName(any(String.class))).thenReturn(Optional.of(initiativeType));
        when(eventRepo.save(event)).thenReturn(event);
        when(modelMapper.map(event, EventResponseDto.class)).thenReturn(eventResponseDto);
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(ModelUtils.getUser()));
        when(imageRepo.findByImagePath(any(String.class))).thenReturn(Optional.of(new Image()));
        when(modelMapper.map(any(EventDateInfoRequestDto.class), eq(EventDateInfo.class))).thenReturn(new EventDateInfo());
        when(eventDateInfoRepo.save(any(EventDateInfo.class))).thenReturn(new EventDateInfo());
        when(modelMapper.map(any(ImageRequestDto.class), eq(Image.class))).thenReturn(new Image());

        EventResponseDto result = eventService.createEvent(eventRequestDto);

        assertNotNull(result);
        assertEquals(event.getId(), result.getId());
        assertEquals(event.getTitle(), result.getTitle());
        assertEquals(event.getDescription(), result.getDescription());

        verify(eventRepo, times(1)).save(event);
    }

    @Test
    void getEventById_ShouldReturnEventResponseDto_WhenEventExists() {
        eventResponseDto.setJoined(true);
        when(eventRepo.findById(1L)).thenReturn(Optional.of(event));
        when(modelMapper.map(event, EventResponseDto.class)).thenReturn(eventResponseDto);

        Optional<EventResponseDto> result = eventService.getEventById(1L, "email@gmail.com");

        assertTrue(result.isPresent());
        assertEquals(event.getId(), result.get().getId());
        assertTrue(result.get().isJoined());
        verify(eventRepo, times(1)).findById(1L);
    }

    @Test
    void getEventByIdNotFound() {
        when(eventRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getEventById(1L, "email@gmail.com"));
    }

    @Test
    void getAllEvents_ShouldReturnListOfEventResponseDto() {
        when(eventRepo.findAll()).thenReturn(List.of(event));
        when(modelMapper.map(event, EventResponseDto.class)).thenReturn(eventResponseDto);

        List<EventResponseDto> result = eventService.getAllEvents();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(eventRepo, times(1)).findAll();
    }

    @Test
    void createEvent_ShouldThrowIllegalArgumentException_WhenEventDaysAreEmpty() {
        eventRequestDto.setEventDays(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            eventService.createEvent(eventRequestDto);
        });

        assertEquals("Event must have at least one event day.", exception.getMessage());
    }

    @Test
    void getAllUserEventsTest() {
        User mockUser = ModelUtils.getUser();
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.of(mockUser));

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        List<Event> content = List.of(event1, event2);
        Page<Event> events = new PageImpl<>(content);
        Pageable pageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "id"));

        when(eventRepo.findAllByAuthorOrParticipant(anyLong(), eq(pageable))).thenReturn(events);
        when(eventDateInfoRepo.findByEvent(any(Event.class))).thenReturn(List.of(new EventDateInfo()));
        when(participationRepo.findUsersByEventId(anyLong())).thenReturn(List.of());

        EventProfilePreviewDto mockDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Mock Event Dto")
            .build();

        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenReturn(mockDto);

        EventProfilePreviewPageable result = eventService.getAllUserEvents(mockUser.getEmail(), pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Mock Event Dto", result.getContent().get(0).getTitle());

        verify(userRepo, times(1)).findByEmail(anyString());
        verify(eventRepo, times(1)).findAllByAuthorOrParticipant(anyLong(), eq(pageable));
        verify(eventDateInfoRepo, times(2)).findByEvent(any(Event.class));
        verify(participationRepo, times(2)).findUsersByEventId(anyLong());
        verify(modelMapper, times(2)).map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class));
    }

    @Test
    void getAllUserEventsNoUserFoundTest() {
        when(userRepo.findByEmail(any(String.class))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getAllUserEvents("a@gmail.com", Pageable.unpaged()));

        verify(userRepo, times(1)).findByEmail(anyString());
        verify(eventRepo, never()).findAllByAuthorOrParticipant(anyLong(), any(Pageable.class));
    }

    @Test
    void getAllUserPastEvents_ShouldReturnEventProfilePreviewPageable() {
        String userEmail = "user@example.com";
        User user = new User();
        user.setId(1L);
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        List<Event> content = List.of(event1, event2);
        Page<Event> events = new PageImpl<>(content);
        Pageable pageable = PageRequest.of(0, 3);

        when(eventRepo.findUserEventsByTime(eq(user.getId()), any(LocalDateTime.class), eq("PAST"), eq(pageable)))
            .thenReturn(events);
        when(eventDateInfoRepo.findByEvent(any(Event.class))).thenReturn(List.of(new EventDateInfo()));
        when(participationRepo.findUsersByEventId(anyLong())).thenReturn(List.of());

        EventProfilePreviewDto mockDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Mock Past Event Dto")
            .build();
        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenReturn(mockDto);

        EventProfilePreviewPageable result = eventService.getAllUserPastEvents(userEmail, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Mock Past Event Dto", result.getContent().get(0).getTitle());

        verify(userRepo, times(1)).findByEmail(userEmail);
        verify(eventRepo, times(1)).findUserEventsByTime(eq(user.getId()), any(LocalDateTime.class), eq("PAST"),
            eq(pageable));
    }

    @Test
    void getAllUserLiveEvents_ShouldReturnEventProfilePreviewPageable() {
        String userEmail = "user@example.com";
        User user = new User();
        user.setId(1L);
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        List<Event> content = List.of(event1, event2);
        Page<Event> events = new PageImpl<>(content);
        Pageable pageable = PageRequest.of(0, 3);

        when(eventRepo.findUserEventsByTime(eq(user.getId()), any(LocalDateTime.class), eq("LIVE"), eq(pageable)))
            .thenReturn(events);
        when(eventDateInfoRepo.findByEvent(any(Event.class))).thenReturn(List.of(new EventDateInfo()));
        when(participationRepo.findUsersByEventId(anyLong())).thenReturn(List.of());

        EventProfilePreviewDto mockDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Mock Live Event Dto")
            .build();
        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenReturn(mockDto);

        EventProfilePreviewPageable result = eventService.getAllUserLiveEvents(userEmail, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Mock Live Event Dto", result.getContent().get(0).getTitle());

        verify(userRepo, times(1)).findByEmail(userEmail);
        verify(eventRepo, times(1)).findUserEventsByTime(eq(user.getId()), any(LocalDateTime.class), eq("LIVE"),
            eq(pageable));
    }

    @Test
    void getAllUserUpcomingEvents_ShouldReturnEventProfilePreviewPageable() {
        String userEmail = "user@example.com";
        User user = new User();
        user.setId(1L);
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        List<Event> content = List.of(event1, event2);
        Page<Event> events = new PageImpl<>(content);
        Pageable pageable = PageRequest.of(0, 3);

        when(eventRepo.findUserEventsByTime(eq(user.getId()), any(LocalDateTime.class), eq("UPCOMING"), eq(pageable)))
            .thenReturn(events);
        when(eventDateInfoRepo.findByEvent(any(Event.class))).thenReturn(List.of(new EventDateInfo()));
        when(participationRepo.findUsersByEventId(anyLong())).thenReturn(List.of());

        EventProfilePreviewDto mockDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Mock Upcoming Event Dto")
            .build();
        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenReturn(mockDto);

        EventProfilePreviewPageable result = eventService.getAllUserUpcomingEvents(userEmail, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Mock Upcoming Event Dto", result.getContent().get(0).getTitle());

        verify(userRepo, times(1)).findByEmail(userEmail);
        verify(eventRepo, times(1)).findUserEventsByTime(eq(user.getId()), any(LocalDateTime.class), eq("UPCOMING"),
            eq(pageable));
    }

    @Test
    void getAllUserEventsByStatus_ShouldReturnEventProfilePreviewPageable() {
        String userEmail = "user@example.com";
        User user = new User();
        user.setId(1L);
        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        EventDateInfo eventDateInfo = new EventDateInfo();
        eventDateInfo.setId(1L);
        eventDateInfo.setEvent(event1);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setId(2L);
        eventDateInfo2.setEvent(event2);

        when(eventDateInfoRepo.findByEvent(any(Event.class))).thenReturn(List.of(eventDateInfo));

        List<Event> content = List.of(event1, event2);
        Page<Event> events = new PageImpl<>(content);
        Pageable pageable = PageRequest.of(0, 3);

        boolean isOnline = true;
        when(eventRepo.findEventsByAuthorAndFirstDayOnlineStatus(eq(user.getId()), eq(isOnline), eq(pageable)))
            .thenReturn(events);

        EventProfilePreviewDto mockDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Mock Event Dto")
            .build();
        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenReturn(mockDto);

        EventProfilePreviewPageable result = eventService.getAllUserEventsByStatus(userEmail, "online", pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Mock Event Dto", result.getContent().get(0).getTitle());

        verify(userRepo, times(1)).findByEmail(userEmail);
        verify(eventRepo, times(1)).findEventsByAuthorAndFirstDayOnlineStatus(eq(user.getId()), eq(isOnline),
            eq(pageable));
        verify(eventDateInfoRepo, times(2)).findByEvent(any(Event.class));
        verify(participationRepo, times(2)).findUsersByEventId(anyLong());
        verify(modelMapper, times(2)).map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class));
    }

    @Test
    public void testUpdateEventSuccess() {

        String userEmail = "user@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(userEmail);
        user.setRole(Role.ROLE_USER);

        when(userRepo.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Long eventId = 1L;
        EventUpdateDto eventUpdateDto = new EventUpdateDto();
        eventUpdateDto.setAuthorEmail(userEmail);
        eventUpdateDto.setTitle("New Event Title");
        eventUpdateDto.setDescription("New Event Description");

        EventDateInfoUpdateDto eventDateInfoUpdateDto = new EventDateInfoUpdateDto();
        eventDateInfoUpdateDto.setEventDate(LocalDate.now().plusDays(1));
        eventDateInfoUpdateDto.setEventTimeStart(LocalDateTime.now().plusHours(1));
        eventDateInfoUpdateDto.setEventTimeEnd(LocalDateTime.now().plusHours(2));
        eventDateInfoUpdateDto.setIsAllDay(false);
        eventDateInfoUpdateDto.setIsPlace(true);
        eventDateInfoUpdateDto.setIsOnline(false);
        eventDateInfoUpdateDto.setLocation("Sample Location");
        eventDateInfoUpdateDto.setUrl("http://example.com");
        eventDateInfoUpdateDto.setNumOfTheDay(1);

        eventUpdateDto.setEventDays(List.of(eventDateInfoUpdateDto));

        InitiativeTypeRequestDto initiativeTypeRequestDto = new InitiativeTypeRequestDto();
        initiativeTypeRequestDto.setName("Environmental");

        eventUpdateDto.setInitiativeTypes(List.of(initiativeTypeRequestDto));

        Event existingEvent = new Event();
        existingEvent.setId(eventId);
        existingEvent.setTitle("Old Event Title");
        existingEvent.setCreationDate(ZonedDateTime.now());
        existingEvent.setDescription("Old Event Description");
        existingEvent.setDuration(120);
        existingEvent.setLikes(100);
        existingEvent.setRating(4.5);
        existingEvent.setAuthor(user);

        InitiativeType initiativeType = new InitiativeType();
        initiativeType.setId(1L);
        initiativeType.setName("Social");

        existingEvent.setInitiativeTypes(List.of(initiativeType));

        Image image1 = new Image();
        image1.setId(1L);
        Image image2 = new Image();
        image2.setId(2L);
        Set<Image> images = new HashSet<>();
        images.add(image1);
        images.add(image2);

        existingEvent.setImages(images);

        Image mainImage = new Image();
        mainImage.setId(3L);

        existingEvent.setMainImage(mainImage);

        InitiativeType environmentalInitiativeType = new InitiativeType();
        environmentalInitiativeType.setId(2L);
        environmentalInitiativeType.setName("Environmental");

        when(initiativeTypeRepo.findByName("Environmental")).thenReturn(Optional.of(environmentalInitiativeType));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(existingEvent));

        Image defaultImage = new Image();
        defaultImage.setId(1L);
        when(imageRepo.findById(1L)).thenReturn(Optional.of(defaultImage));

        EventDateInfo eventDateInfo = new EventDateInfo();
        eventDateInfo.setId(1L);
        eventDateInfo.setEvent(existingEvent);
        eventDateInfo.setLocation("Sample Location");
        eventDateInfo.setEventTimeStart(LocalDateTime.now().plusHours(1));
        eventDateInfo.setEventTimeEnd(LocalDateTime.now().plusHours(2));

        when(eventDateInfoRepo.findById(1L)).thenReturn(Optional.of(eventDateInfo));
        when(modelMapper.map(any(Event.class), eq(EventResponseDto.class)))
            .thenReturn(new EventResponseDto());
        when(modelMapper.map(any(EventDateInfo.class), eq(EventDateInfoResponseDto.class)))
            .thenReturn(eventDateInfoResponseDto);
        when(modelMapper.map(any(EventDateInfoUpdateDto.class), eq(EventDateInfo.class))).thenReturn(eventDateInfo);
        when(eventLikesRepo.countLikesByEventId(eventId)).thenReturn(150);
        when(eventDateInfoRepo.findByEvent(existingEvent)).thenReturn(List.of(eventDateInfo));

        EventResponseDto result = eventService.updateEvent(eventId, eventUpdateDto, userEmail);

        EventDateInfo updatedDateInfo = eventDateInfoRepo.findById(eventId).orElseThrow();

        assertNotNull(result);

        assertEquals("New Event Title", existingEvent.getTitle());
        assertEquals("New Event Description", existingEvent.getDescription());
        assertEquals("Sample Location", updatedDateInfo.getLocation());
        assertEquals("Environmental", existingEvent.getInitiativeTypes().get(0).getName());

        verify(eventRepo, times(4)).findById(eventId);
        verify(imageRepo, times(2)).findById(1L);
        verify(eventDateInfoRepo, times(2)).findById(1L);
        verify(eventDateInfoService).updateEventDateInfo(anyLong(), any(EventDateInfoUpdateDto.class));
        verify(initiativeTypeRepo).findByName("Environmental");
        verify(eventLikesRepo).countLikesByEventId(eventId);
    }

    @Test
    public void getAllEventsPageableTest() {
        Pageable pageable = PageRequest.of(0, 10);

        Event event1 = new Event();
        event1.setId(1L);
        Event event2 = new Event();
        event2.setId(2L);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setEventTimeStart(LocalDateTime.now().plusHours(1));
        eventDateInfo1.setEventTimeEnd(LocalDateTime.now().plusHours(2));

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setEventTimeStart(LocalDateTime.now().plusDays(1));
        eventDateInfo2.setEventTimeEnd(LocalDateTime.now().plusDays(1).plusHours(2));

        Page<Event> page = new PageImpl<Event>(List.of(event1, event2));

        when(eventRepo.findAllSortedByStartDateAsc(pageable)).thenReturn(page);
        when(eventDateInfoRepo.findByEvent(event1)).thenReturn(List.of(eventDateInfo1));
        when(eventDateInfoRepo.findByEvent(event2)).thenReturn(List.of(eventDateInfo2));
        when(participationRepo.findUsersByEventId(anyLong())).thenReturn(List.of());

        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenAnswer(invocation -> {
                EventMappingContext context = invocation.getArgument(0);
                return EventProfilePreviewDto.builder()
                    .id(context.getEvent().getId())
                    .title("Mapped Event " + context.getEvent().getId())
                    .build();
            });

        EventProfilePreviewPageable result = eventService.getAllEventsPageable(pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Mapped Event 1", result.getContent().get(0).getTitle());

        verify(eventRepo, times(1)).findAllSortedByStartDateAsc(pageable);
        verify(eventDateInfoRepo, times(2)).findByEvent(any(Event.class));
        verify(participationRepo, times(2)).findUsersByEventId(anyLong());
        verify(modelMapper, times(2)).map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class));
    }

    @Test
    void testGetEventsByTitle_ReturnsEmptyPageable() {
        String title = "non-existing";
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepo.findByTitleContainingIgnoreCaseSortedByTitle(title, pageable))
            .thenReturn(Page.empty());

        EventProfilePreviewPageable result = eventService.getEventsByTitle(title, pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testGetEventsByTitle_ReturnsPopulatedPageable() {
        String title = "Test Event";
        Pageable pageable = PageRequest.of(0, 10);

        Event event = new Event();
        event.setId(1L);
        event.setTitle(title);
        List<Event> events = List.of(event);

        Page<Event> eventPage = new PageImpl<>(events);
        when(eventRepo.findByTitleContainingIgnoreCaseSortedByTitle(title, pageable))
            .thenReturn(eventPage);

        EventDateInfo eventDateInfo = new EventDateInfo();
        when(eventDateInfoRepo.findByEvent(event)).thenReturn(List.of(eventDateInfo));

        when(participationRepo.findUsersByEventId(event.getId())).thenReturn(Collections.emptyList());

        EventProfilePreviewDto dto = new EventProfilePreviewDto();
        when(modelMapper.map(any(EventMappingContext.class), eq(EventProfilePreviewDto.class)))
            .thenReturn(dto);

        EventProfilePreviewPageable result = eventService.getEventsByTitle(title, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
