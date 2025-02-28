package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.event.*;
import greencity.dto.user.AuthorDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.EventService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static greencity.ModelUtils.getPrincipal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {
    @Mock
    private EventService eventService;

    private final Principal principal = getPrincipal();

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ErrorAttributes errorAttributes;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventController eventController;

    private EventRequestDto eventRequestDto;
    private ObjectMapper objectMapper2;
    private MockMvc mockMvc;
    private EventResponseDto eventResponseDto;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();

        attributes.put("timestamp", "2025-01-13T10:00:00");
        attributes.put("trace", "Test stack trace");

        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();

        objectMapper2 = new ObjectMapper();
        objectMapper2.registerModule(new JavaTimeModule());
        objectMapper2.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        eventRequestDto = new EventRequestDto();
        eventRequestDto.setAuthorEmail(principal.getName());
        eventRequestDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());
        eventRequestDto.setTitle("title");
        eventRequestDto.setDescription("descriptionglygluigluyfluyflukyfkytfkytdkytdkytdkytkyt");
        eventRequestDto.setInitiativeTypes(List.of(InitiativeTypeRequestDto.builder().name("Economic").build()));
        eventRequestDto.setOpen(true);
        eventRequestDto.setDuration(1);
        eventRequestDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());

        EventDateInfoRequestDto eventDateInfoRequestDto = new EventDateInfoRequestDto();
        eventDateInfoRequestDto.setIsOnline(true);
        eventDateInfoRequestDto.setIsAllDay(false);
        eventDateInfoRequestDto.setUrl("http://google.com");
        eventDateInfoRequestDto.setIsPlace(false);
        eventDateInfoRequestDto.setEventDate(LocalDate.of(2025, 12, 15));
        eventDateInfoRequestDto.setEventTimeStart(LocalDateTime.of(2025, 12, 15, 14, 30));
        eventDateInfoRequestDto.setEventTimeEnd(LocalDateTime.of(2025, 12, 15, 15, 30));

        eventRequestDto.setEventDays(List.of(eventDateInfoRequestDto));

        eventResponseDto = new EventResponseDto();
        eventResponseDto.setId(1L);
        eventResponseDto.setTitle("title");
        eventResponseDto.setDescription("descriptionglygluigluyfluyflukyfkytfkytdkytdkytdkytkyt");
        eventResponseDto.setOpen(true);
        eventResponseDto.setDuration(1);
        eventResponseDto.setMainImage(ImageResponseDto.builder().imagePath("imagePath").build());
        eventResponseDto.setAuthor(AuthorDto.builder().name("Masha").id(12L).build());
    }

    @Test
    void createTest() throws Exception {

        when(eventService.createEvent(any(EventRequestDto.class))).thenReturn(eventResponseDto);

        MvcResult result = mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper2.writeValueAsString(eventRequestDto))
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventResponseDto.getId()))
                .andExpect(jsonPath("$.title").value(eventResponseDto.getTitle()))
                .andExpect(jsonPath("$.description").value(eventResponseDto.getDescription()))
                .andExpect(jsonPath("$.open").value(eventResponseDto.isOpen()))
                .andExpect(jsonPath("$.duration").value(eventResponseDto.getDuration()))
                .andExpect(jsonPath("$.author.name").value(eventResponseDto.getAuthor().getName()))
                .andExpect(jsonPath("$.author.id").value(eventResponseDto.getAuthor().getId()))
                .andExpect(jsonPath("$.mainImage.imagePath").value(eventResponseDto.getMainImage().getImagePath()))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains("title"));

        verify(eventService, times(1)).createEvent(any(EventRequestDto.class));
    }

    @Test
    void createTestNoTitle() throws Exception {
        eventRequestDto.setTitle(null);

        mockMvc.perform(post("/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper2.writeValueAsString(eventRequestDto))
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors[0].message").value("Title cannot be empty"))
            .andExpect(jsonPath("$.errors[0].field").value("title"))
            .andReturn();

        verify(eventService, times(0)).createEvent(any(EventRequestDto.class));
    }

    @Test
    void getEventsByUserTest() throws Exception {
        EventProfilePreviewDto eventProfilePreviewDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Sample Event")
            .creationDate(ZonedDateTime.now())
            .eventDate(LocalDate.now().plusDays(5))
            .eventTimeStart(LocalDateTime.now().plusHours(3))
            .author(new AuthorDto(1L, "John Doe"))
            .location("Online")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(true)
            .mainImage(new ImageResponseDto(1L, "https://example.com/image.jpg"))
            .rating(4.5)
            .participants(List.of(new UserProfilePictureDto(1L, "Maria", "https://example.com/user1.jpg")))
            .build();

        EventProfilePreviewDto eventProfilePreviewDto2 = EventProfilePreviewDto.builder()
            .id(2L)
            .title("Eco Conference 2025")
            .creationDate(ZonedDateTime.now().minusDays(2))
            .eventDate(LocalDate.now().plusDays(10))
            .eventTimeStart(LocalDateTime.now().plusHours(5))
            .author(new AuthorDto(2L, "Alice Johnson"))
            .location("Kharkiv")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(false)
            .mainImage(new ImageResponseDto(2L, "https://example.com/event2.jpg"))
            .rating(4.8)
            .participants(List.of(
                new UserProfilePictureDto(2L, "David", "https://example.com/user2.jpg"),
                new UserProfilePictureDto(3L, "Sophia", "https://example.com/user3.jpg")))
            .build();

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            List.of(eventProfilePreviewDto, eventProfilePreviewDto2),
            0, 3, 10L, 5, false);

        when(eventService.getAllUserEvents(anyString(), any(Pageable.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/myEvents")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "3")
            .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPageNo());
        assertEquals(3, response.getPageSize());
        assertEquals(10L, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertFalse(response.isLast());

        verify(eventService, times(1)).getAllUserEvents(anyString(), any(Pageable.class));
    }

    @Test
    void getEventsByUserNotFoundTest() throws Exception {
        when(eventService.getAllUserEvents(anyString(), any(Pageable.class))).thenThrow(new NotFoundException("User not found: " + ModelUtils.getPrincipal().getName()));

        attributes.put("path", "/events/myEvents");
        attributes.put("message", "User not found: " + ModelUtils.getPrincipal().getName());
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class))).thenReturn(attributes);
        mockMvc.perform(get("/events/myEvents")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "3")
                        .param("sort", "id,desc"))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void getEventByIdTest() throws Exception {
        eventResponseDto.setParticipants(
            List.of(UserProfilePictureDto.builder().id(1L).name("Masha").profilePicturePath("picture").build()));
        when(eventService.getEventById(anyLong(), anyString())).thenReturn(Optional.of(eventResponseDto));

        MvcResult result = mockMvc.perform(get("/events/1")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        EventResponseDto response = objectMapper2.readValue(jsonResponse, EventResponseDto.class);

        assertNotNull(jsonResponse);
        assertEquals(1, response.getParticipants().size());

        verify(eventService, times(1)).getEventById(anyLong(), anyString());
    }

    @Test
    void getEventByIdEventNotFoundTest() throws Exception {
        when(eventService.getEventById(anyLong(), anyString())).thenThrow(new NotFoundException("Event not found"));

        attributes.put("path", "/events/1");
        attributes.put("message", "Event not found");
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class))).thenReturn(attributes);

        mockMvc.perform(get("/events/1")
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void getPastEventsByUserTest() throws Exception {
        EventProfilePreviewDto eventProfilePreviewDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Past Event")
            .creationDate(ZonedDateTime.now().minusDays(5))
            .eventDate(LocalDate.now().minusDays(3))
            .eventTimeStart(LocalDateTime.now().minusHours(2))
            .author(new AuthorDto(1L, "John Doe"))
            .location("Online")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(true)
            .mainImage(new ImageResponseDto(1L, "https://example.com/image.jpg"))
            .rating(4.5)
            .participants(List.of(new UserProfilePictureDto(1L, "Maria", "https://example.com/user1.jpg")))
            .build();

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            List.of(eventProfilePreviewDto),
            0, 1, 1L, 1, true);

        when(eventService.getAllUserPastEvents(anyString(), any(Pageable.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/myEvents/past")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNo());
        assertEquals(1, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getAllUserPastEvents(anyString(), any(Pageable.class));
    }

    @Test
    void getLiveEventsByUserTest() throws Exception {
        EventProfilePreviewDto eventProfilePreviewDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Live Event")
            .creationDate(ZonedDateTime.now().minusDays(1))
            .eventDate(LocalDate.now())
            .eventTimeStart(LocalDateTime.now().plusHours(1))
            .author(new AuthorDto(1L, "John Doe"))
            .location("Online")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(true)
            .mainImage(new ImageResponseDto(1L, "https://example.com/image.jpg"))
            .rating(4.5)
            .participants(List.of(new UserProfilePictureDto(1L, "Maria", "https://example.com/user1.jpg")))
            .build();

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            List.of(eventProfilePreviewDto),
            0, 1, 1L, 1, true);

        when(eventService.getAllUserLiveEvents(anyString(), any(Pageable.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/myEvents/live")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNo());
        assertEquals(1, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getAllUserLiveEvents(anyString(), any(Pageable.class));
    }

    @Test
    void getUpcomingEventsByUserTest() throws Exception {
        EventProfilePreviewDto eventProfilePreviewDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Upcoming Event")
            .creationDate(ZonedDateTime.now().minusDays(2))
            .eventDate(LocalDate.now().plusDays(5))
            .eventTimeStart(LocalDateTime.now().plusDays(1).plusHours(2))
            .author(new AuthorDto(1L, "John Doe"))
            .location("Kharkiv")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(true)
            .mainImage(new ImageResponseDto(1L, "https://example.com/image.jpg"))
            .rating(4.8)
            .participants(List.of(new UserProfilePictureDto(1L, "Maria", "https://example.com/user1.jpg")))
            .build();

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            List.of(eventProfilePreviewDto),
            0, 1, 1L, 1, true);

        when(eventService.getAllUserUpcomingEvents(anyString(), any(Pageable.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/myEvents/upcoming")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNo());
        assertEquals(1, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getAllUserUpcomingEvents(anyString(), any(Pageable.class));
    }

    @Test
    void getUpcomingEventsByUser_EmptyListTest() throws Exception {
        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            Collections.emptyList(),
            0, 1, 0L, 0, true);

        when(eventService.getAllUserUpcomingEvents(anyString(), any(Pageable.class)))
            .thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/myEvents/upcoming")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(0, response.getContent().size());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getAllUserUpcomingEvents(anyString(), any(Pageable.class));
    }

    @Test
    void getAllUserEventsByStatusTest() throws Exception {

        String status = "online";
        String userEmail = principal.getName();

        EventProfilePreviewPageable eventProfilePreviewPageable = new EventProfilePreviewPageable(
            List.of(new EventProfilePreviewDto()),
            0, 1, 1L, 1, true);

        Pageable pageable = PageRequest.of(0, 1);

        when(eventService.getAllUserEventsByStatus(userEmail, status, pageable))
            .thenReturn(eventProfilePreviewPageable);

        mockMvc.perform(get("/events/myEvents/status/{status}", status)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
            .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void updateTest() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventUpdateDto.class), anyString())).thenReturn(eventResponseDto);

        EventUpdateDto eventUpdateDto = new EventUpdateDto();

        eventUpdateDto.setAuthorEmail(principal.getName());
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());
        eventUpdateDto.setTitle("title");
        eventUpdateDto.setDescription("descriptionglygluigluyfluyflukyfkytfkytdkytdkytdkytkyt");
        eventUpdateDto.setInitiativeTypes(List.of(InitiativeTypeRequestDto.builder().name("Economic").build()));
        eventUpdateDto.setOpen(true);
        eventUpdateDto.setDuration(1);
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());

        EventDateInfoUpdateDto eventDateInfoUpdateDto = new EventDateInfoUpdateDto();
        eventDateInfoUpdateDto.setIsOnline(true);
        eventDateInfoUpdateDto.setIsAllDay(false);
        eventDateInfoUpdateDto.setUrl("http://google.com");
        eventDateInfoUpdateDto.setIsPlace(false);
        eventDateInfoUpdateDto.setEventDate(LocalDate.of(2025, 12, 15));
        eventDateInfoUpdateDto.setEventTimeStart(LocalDateTime.of(2025, 12, 15, 14, 30));
        eventDateInfoUpdateDto.setEventTimeEnd(LocalDateTime.of(2025, 12, 15, 15, 30));

        eventUpdateDto.setEventDays(List.of(eventDateInfoUpdateDto));

        MvcResult result = mockMvc.perform(put("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper2.writeValueAsString(eventUpdateDto))
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventResponseDto.getId()))
                .andExpect(jsonPath("$.title").value(eventResponseDto.getTitle()))
                .andExpect(jsonPath("$.description").value(eventResponseDto.getDescription()))
                .andExpect(jsonPath("$.open").value(eventResponseDto.isOpen()))
                .andExpect(jsonPath("$.duration").value(eventResponseDto.getDuration()))
                .andExpect(jsonPath("$.author.name").value(eventResponseDto.getAuthor().getName()))
                .andExpect(jsonPath("$.author.id").value(eventResponseDto.getAuthor().getId()))
                .andExpect(jsonPath("$.mainImage.imagePath").value(eventResponseDto.getMainImage().getImagePath()))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains("title"));
        assertTrue(responseContent.contains("description"));
        assertTrue(responseContent.contains("open"));

        verify(eventService, times(1)).updateEvent(eq(1L), any(EventUpdateDto.class), eq(principal.getName()));
    }

    @Test
    void updateNoRequiredFieldTitleTest() throws Exception {
        EventUpdateDto eventUpdateDto = new EventUpdateDto();

        eventUpdateDto.setAuthorEmail(principal.getName());
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());
        eventUpdateDto.setDescription("descriptionglygluigluyfluyflukyfkytfkytdkytdkytdkytkyt");
        eventUpdateDto.setInitiativeTypes(List.of(InitiativeTypeRequestDto.builder().name("Economic").build()));
        eventUpdateDto.setOpen(true);
        eventUpdateDto.setDuration(1);
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());

        mockMvc.perform(put("/events/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper2.writeValueAsString(eventUpdateDto))
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();

        verify(eventService, times(0)).updateEvent(eq(1L), any(EventUpdateDto.class), eq(principal.getName()));
    }

    @Test
    void updateNoRequiredFieldEventDaysTest() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventUpdateDto.class), anyString())).thenThrow(new IllegalArgumentException("Event must have at least one event day."));

        EventUpdateDto eventUpdateDto = new EventUpdateDto();

        eventUpdateDto.setAuthorEmail(principal.getName());
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());
        eventUpdateDto.setTitle("title");
        eventUpdateDto.setDescription("descriptionglygluigluyfluyflukyfkytfkytdkytdkytdkytkyt");
        eventUpdateDto.setInitiativeTypes(List.of(InitiativeTypeRequestDto.builder().name("Economic").build()));
        eventUpdateDto.setOpen(true);
        eventUpdateDto.setDuration(1);
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());

        MvcResult result = mockMvc.perform(put("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper2.writeValueAsString(eventUpdateDto))
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("Event must have at least one event day."), "Error message should be in the response");

        verify(eventService, times(1)).updateEvent(eq(1L), any(EventUpdateDto.class), eq(principal.getName()));
    }

    @Test
    void updateInThePastTest() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventUpdateDto.class), anyString())).thenThrow(new BadRequestException("You cannot edit the event that is in the past"));

        EventUpdateDto eventUpdateDto = new EventUpdateDto();

        eventUpdateDto.setAuthorEmail(principal.getName());
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());
        eventUpdateDto.setTitle("title");
        eventUpdateDto.setDescription("descriptionglygluigluyfluyflukyfkytfkytdkytdkytdkytkyt");
        eventUpdateDto.setInitiativeTypes(List.of(InitiativeTypeRequestDto.builder().name("Economic").build()));
        eventUpdateDto.setOpen(true);
        eventUpdateDto.setDuration(1);
        eventUpdateDto.setMainImage(ImageRequestDto.builder().imagePath("imagePath").build());

        EventDateInfoUpdateDto eventDateInfoUpdateDto = new EventDateInfoUpdateDto();
        eventDateInfoUpdateDto.setIsOnline(true);
        eventDateInfoUpdateDto.setIsAllDay(false);
        eventDateInfoUpdateDto.setUrl("http://google.com");
        eventDateInfoUpdateDto.setIsPlace(false);
        eventDateInfoUpdateDto.setEventDate(LocalDate.of(2025, 12, 15));
        eventDateInfoUpdateDto.setEventTimeStart(LocalDateTime.of(2025, 12, 15, 14, 30));
        eventDateInfoUpdateDto.setEventTimeEnd(LocalDateTime.of(2025, 12, 15, 15, 30));

        eventUpdateDto.setEventDays(List.of(eventDateInfoUpdateDto));

        attributes.put("path", "/events/1");
        attributes.put("message", "You cannot edit the event that is in the past");
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class))).thenReturn(attributes);

        MvcResult result = mockMvc.perform(put("/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper2.writeValueAsString(eventUpdateDto))
                        .principal(principal)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertTrue(responseContent.contains("You cannot edit the event that is in the past"), "Error message should be in the response");

        verify(eventService, times(1)).updateEvent(eq(1L), any(EventUpdateDto.class), eq(principal.getName()));
    }

    @Test
    void getAllEventsTest() throws Exception {
        EventProfilePreviewDto eventProfilePreviewDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Sample Event")
            .creationDate(ZonedDateTime.now())
            .eventDate(LocalDate.now().plusDays(5))
            .eventTimeStart(LocalDateTime.now().plusHours(3))
            .author(new AuthorDto(1L, "John Doe"))
            .location("Online")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(true)
            .mainImage(new ImageResponseDto(1L, "https://example.com/image.jpg"))
            .rating(4.5)
            .participants(List.of(new UserProfilePictureDto(1L, "Maria", "https://example.com/user1.jpg")))
            .build();

        EventProfilePreviewDto eventProfilePreviewDto2 = EventProfilePreviewDto.builder()
            .id(2L)
            .title("Eco Conference 2025")
            .creationDate(ZonedDateTime.now().minusDays(2))
            .eventDate(LocalDate.now().plusDays(10))
            .eventTimeStart(LocalDateTime.now().plusHours(5))
            .author(new AuthorDto(2L, "Alice Johnson"))
            .location("Kharkiv")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(false)
            .mainImage(new ImageResponseDto(2L, "https://example.com/event2.jpg"))
            .rating(4.8)
            .participants(List.of(
                new UserProfilePictureDto(2L, "David", "https://example.com/user2.jpg"),
                new UserProfilePictureDto(3L, "Sophia", "https://example.com/user3.jpg")))
            .build();

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            List.of(eventProfilePreviewDto, eventProfilePreviewDto2),
            0, 3, 10L, 5, false);

        when(eventService.getAllEventsPageable(any(Pageable.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events")
            .accept(MediaType.APPLICATION_JSON)
            .param("page", "0")
            .param("size", "3"))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(2, response.getContent().size());
        assertEquals(0, response.getPageNo());
        assertEquals(3, response.getPageSize());
        assertEquals(10L, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertFalse(response.isLast());

        verify(eventService, times(1)).getAllEventsPageable(any(Pageable.class));
    }

    @Test
    void searchEventByTitleTest() throws Exception {
        String title = "Upcoming Event";
        EventProfilePreviewDto eventProfilePreviewDto = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Upcoming Event")
            .creationDate(ZonedDateTime.now().minusDays(2))
            .eventDate(LocalDate.now().plusDays(5))
            .eventTimeStart(LocalDateTime.now().plusDays(1).plusHours(2))
            .author(new AuthorDto(1L, "John Doe"))
            .location("Kharkiv")
            .initiativeTypes(List.of(new InitiativeTypeResponseDto(3L, "Economic")))
            .isOpen(true)
            .mainImage(new ImageResponseDto(1L, "https://example.com/image.jpg"))
            .rating(4.8)
            .participants(List.of(new UserProfilePictureDto(1L, "Maria", "https://example.com/user1.jpg")))
            .build();

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            List.of(eventProfilePreviewDto),
            0, 1, 1L, 1, true);

        when(eventService.getEventsByTitle(eq(title), any(Pageable.class))).thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/search")
            .param("title", title)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(1, response.getContent().size());
        assertEquals(0, response.getPageNo());
        assertEquals(1, response.getPageSize());
        assertEquals(1L, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getEventsByTitle(eq(title), any(Pageable.class));
    }

    @Test
    void searchEventByTitle_EmptyListTest() throws Exception {
        String title = "Non-existent Event";

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            Collections.emptyList(),
            0, 1, 0L, 0, true);

        when(eventService.getEventsByTitle(eq(title), any(Pageable.class)))
            .thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/search")
            .param("title", title)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(0, response.getContent().size());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getEventsByTitle(eq(title), any(Pageable.class));
    }

    @Test
    void searchEventByTitle_InvalidTitleTest() throws Exception {
        String invalidTitle = "";

        EventProfilePreviewPageable result = new EventProfilePreviewPageable(
            Collections.emptyList(),
            0, 1, 0L, 0, true);

        when(eventService.getEventsByTitle(eq(invalidTitle), any(Pageable.class)))
            .thenReturn(result);

        MvcResult mvcResult = mockMvc.perform(get("/events/search")
            .param("title", invalidTitle)
            .param("page", "0")
            .param("size", "1")
            .param("sort", "id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        EventProfilePreviewPageable response = objectMapper2.readValue(jsonResponse, EventProfilePreviewPageable.class);

        assertEquals(0, response.getContent().size());
        assertEquals(0L, response.getTotalElements());
        assertEquals(0, response.getTotalPages());
        assertTrue(response.isLast());

        verify(eventService, times(1)).getEventsByTitle(eq(invalidTitle), any(Pageable.class));
    }
}
