package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventCommentDtoResponse;
import greencity.dto.event.EventCommentRequestDto;
import greencity.dto.event.EventCommentResponseDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.entity.Event;
import greencity.entity.EventComment;
import greencity.entity.User;
import greencity.entity.VerifyEmail;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.repository.EventCommentRepo;
import greencity.repository.EventRepo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventCommentServiceImplTest {
    @Mock
    private EventRepo eventRepo;

    @Mock
    private EventCommentRepo eventCommentRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventCommentServiceImpl service;

    private Event event;
    private EventComment eventComment;
    private EventComment eventComment2;

    private static User getUser() {
        return User.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .role(Role.ROLE_USER)
            .userStatus(UserStatus.ACTIVATED)
            .lastActivityTime(LocalDateTime.now())
            .verifyEmail(new VerifyEmail())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    }

    private static UserVO getUserVO() {
        return UserVO.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .role(Role.ROLE_USER)
            .lastActivityTime(LocalDateTime.now())
            .verifyEmail(new VerifyEmailVO())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    }

    @BeforeEach
    void setUp() {
        event = new Event();
        event.setId(1L);
        event.setAuthor(new User());

        eventComment = new EventComment();
        eventComment.setId(1L);
        eventComment.setText("text1");
        eventComment.setCreatedDate(LocalDateTime.now());

        eventComment2 = new EventComment();
        eventComment2.setId(2L);
        eventComment2.setText("text2");
        eventComment2.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void getCommentsByEventSortedByNewestFirstTest() {
        EventComment newerComment = new EventComment();
        newerComment.setId(1L);
        newerComment.setText("Newest Comment");
        newerComment.setCreatedDate(LocalDateTime.now().plusMinutes(10));

        EventComment olderComment = new EventComment();
        olderComment.setId(2L);
        olderComment.setText("Older Comment");
        olderComment.setCreatedDate(LocalDateTime.now());

        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.of(event));

        PageRequest pageable = PageRequest.of(0, 10);
        Page<EventComment> eventCommentPage = new PageImpl<>(List.of(newerComment, olderComment), pageable, 2);

        when(eventCommentRepo.findByEventOrderByCreatedDateDesc(any(Event.class), eq(pageable)))
            .thenReturn(eventCommentPage);

        when(modelMapper.map(any(EventComment.class), eq(EventCommentResponseDto.class)))
            .thenAnswer(invocation -> {
                EventComment source = invocation.getArgument(0);
                return EventCommentResponseDto.builder()
                    .id(source.getId())
                    .text(source.getText())
                    .createdDate(source.getCreatedDate())
                    .modifiedDate(source.getModifiedDate())
                    .author(new UserProfilePictureDto())
                    .likes(0)
                    .parentCommentId(null)
                    .build();
            });

        PageableAdvancedDto<EventCommentResponseDto> result = service.getCommentsByEvent(1L, 0, 10);

        verify(eventCommentRepo, times(1)).findByEventOrderByCreatedDateDesc(any(Event.class), eq(pageable));

        Assertions.assertEquals(2, result.getTotalElements());
        Assertions.assertEquals("Newest Comment", result.getContent().get(0).getText());
        Assertions.assertEquals("Older Comment", result.getContent().get(1).getText());
    }

    @Test
    void getCommentsByEventNoEventTest() {
        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> service.getCommentsByEvent(1L, 0, 10));

        verify(eventCommentRepo, times(0)).findByEvent(any(Event.class), any(PageRequest.class));
        Assertions.assertEquals("Event not found with id: 1", exception.getMessage());
    }

    @Test
    void countCommentsByEventTest() {
        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.of(event));
        when(eventCommentRepo.countByEvent(any(Event.class))).thenReturn(66L);

        Long result = service.countCommentsByEvent(1L);

        verify(eventCommentRepo, times(1)).countByEvent(any(Event.class));
        Assertions.assertEquals(66L, result);
    }

    @Test
    void countCommentsByEventNoCommentsTest() {
        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.of(event));
        when(eventCommentRepo.countByEvent(any(Event.class))).thenReturn(0L);

        Long result = service.countCommentsByEvent(1L);

        verify(eventCommentRepo, times(1)).countByEvent(any(Event.class));
        Assertions.assertEquals(0L, result);
    }

    @Test
    void countCommentsByEventNoEventTest() {
        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> service.countCommentsByEvent(1L));

        verify(eventCommentRepo, times(0)).countByEvent(any(Event.class));
        Assertions.assertEquals("Event not found with id: 1", exception.getMessage());
    }

    @Test
    void addCommentTest() {
        EventCommentRequestDto requestDto = new EventCommentRequestDto();
        requestDto.setText("New comment");

        UserVO userVO = getUserVO();
        User user = getUser();
        Event event = new Event();
        event.setId(1L);
        event.setAuthor(getUser());

        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.of(event));
        when(userService.findById(any(Long.class))).thenReturn(userVO);
        when(modelMapper.map(any(UserVO.class), eq(User.class))).thenReturn(user);
        when(eventCommentRepo.save(any(EventComment.class))).thenAnswer(invocation -> {
            EventComment savedComment = invocation.getArgument(0);
            savedComment.setId(100L);
            return savedComment;
        });

        AddEventCommentDtoResponse response = service.addComment(1L, 1L, requestDto);

        verify(eventCommentRepo, times(1)).save(any(EventComment.class));
        Assertions.assertEquals("New comment", response.getText());
        Assertions.assertNotNull(response.getCreatedDate());
    }

    @Test
    void addCommentNoEventTest() {
        when(eventRepo.findById(any(Long.class))).thenReturn(Optional.empty());
        EventCommentRequestDto requestDto = new EventCommentRequestDto();

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
                service.addComment(1L, 1L, requestDto));

        Assertions.assertEquals("Event not found with id: 1", exception.getMessage());
    }

    @Test
    void replyToCommentTest() {
        EventCommentRequestDto requestDto = new EventCommentRequestDto();
        requestDto.setText("Reply text");

        UserVO userVO = new UserVO();
        userVO.setId(2L);
        User user = new User();
        user.setId(2L);

        eventComment.setUser(user);
        eventComment.setEvent(event);

        when(eventCommentRepo.findById(any(Long.class))).thenReturn(Optional.of(eventComment));
        when(userService.findById(any(Long.class))).thenReturn(userVO);
        when(modelMapper.map(any(UserVO.class), eq(User.class))).thenReturn(user);
        when(eventCommentRepo.save(any(EventComment.class))).thenAnswer(invocation -> {
            EventComment savedReply = invocation.getArgument(0);
            savedReply.setId(101L);
            return savedReply;
        });

        AddEventCommentDtoResponse response = service.replyToComment(1L, 2L, requestDto);

        verify(eventCommentRepo, times(1)).save(any(EventComment.class));
        Assertions.assertEquals("Reply text", response.getText());
        Assertions.assertNotNull(response.getCreatedDate());
    }

    @Test
    void getCommentByIdTest() {
        Event event = new Event();
        event.setId(1L);

        eventComment.setEvent(event);

        when(eventCommentRepo.findById(any(Long.class))).thenReturn(Optional.of(eventComment));
        when(modelMapper.map(any(EventComment.class), eq(EventCommentResponseDto.class)))
            .thenAnswer(invocation -> {
                EventComment source = invocation.getArgument(0);
                return EventCommentResponseDto.builder()
                    .id(source.getId())
                    .text(source.getText())
                    .createdDate(source.getCreatedDate())
                    .modifiedDate(source.getModifiedDate())
                    .author(new UserProfilePictureDto())
                    .likes(0)
                    .parentCommentId(null)
                    .build();
            });

        EventCommentResponseDto response = service.getCommentById(1L, 1L);

        verify(eventCommentRepo, times(1)).findById(any(Long.class));
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("text1", response.getText());
    }

    @Test
    void getCommentByIdNotFoundTest() {
        when(eventCommentRepo.findById(any(Long.class))).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(EntityNotFoundException.class, () ->
                service.getCommentById(1L, 2L));

        Assertions.assertEquals("Comment not found with id: 2", exception.getMessage());
    }
}
