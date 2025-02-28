package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventCommentDtoResponse;
import greencity.dto.event.EventCommentRequestDto;
import greencity.dto.event.EventCommentResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.EventCommentService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventCommentControllerTest {
    private static final String EVENT_COMMENT_CONTROLLER_LINK = "/events/{eventId}/comments";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Principal principal = () -> "test@example.com";
    private MockMvc mockMvc;
    @InjectMocks
    private EventCommentController eventCommentController;
    @Mock
    private EventCommentService eventCommentService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;

    private static UserVO getUserVO() {
        return UserVO.builder()
            .id(1L)
            .email("test@example.com")
            .name("Test User")
            .build();
    }

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(eventCommentController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void addCommentTest() throws Exception {
        UserVO userVO = getUserVO();
        EventCommentRequestDto requestDto = new EventCommentRequestDto();
        requestDto.setText("New event comment");
        AddEventCommentDtoResponse responseDto = new AddEventCommentDtoResponse();
        responseDto.setId(1L);
        responseDto.setText("New event comment");

        when(userService.findByEmail(any())).thenReturn(userVO);
        when(eventCommentService.addComment(anyLong(), eq(userVO.getId()), any(EventCommentRequestDto.class)))
            .thenReturn(responseDto);

        mockMvc.perform(post(EVENT_COMMENT_CONTROLLER_LINK, 1)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated());
    }

    @Test
    void addCommentBadRequestTest() throws Exception {
        mockMvc.perform(post(EVENT_COMMENT_CONTROLLER_LINK, 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"text\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void countCommentsTest() throws Exception {
        when(eventCommentService.countCommentsByEvent(anyLong())).thenReturn(5L);

        mockMvc.perform(get(EVENT_COMMENT_CONTROLLER_LINK + "/count", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists())
                .andExpect(jsonPath("$").value(5L));
    }

    @Test
    void getCommentByIdTest() throws Exception {
        EventCommentResponseDto responseDto = new EventCommentResponseDto();
        responseDto.setId(1L);
        responseDto.setText("Sample comment");

        when(eventCommentService.getCommentById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get(EVENT_COMMENT_CONTROLLER_LINK + "/{commentId}", 1, 1)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.text").value("Sample comment"));
    }

    @Test
    void getCommentsByEventTest() throws Exception {
        EventCommentResponseDto responseDto1 = new EventCommentResponseDto();
        responseDto1.setId(1L);
        responseDto1.setText("Comment 1");

        EventCommentResponseDto responseDto2 = new EventCommentResponseDto();
        responseDto2.setId(2L);
        responseDto2.setText("Comment 2");

        List<EventCommentResponseDto> comments = List.of(responseDto1, responseDto2);
        PageableAdvancedDto<EventCommentResponseDto> pageableResponse = new PageableAdvancedDto<>(
            comments, comments.size(), 0, 1, 10, false, false, true, true);

        when(eventCommentService.getCommentsByEvent(anyLong(), anyInt(), anyInt()))
            .thenReturn(pageableResponse);

        mockMvc.perform(get(EVENT_COMMENT_CONTROLLER_LINK, 1)
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(0))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].text").value("Comment 1"))
            .andExpect(jsonPath("$.content[1].id").value(2))
            .andExpect(jsonPath("$.content[1].text").value("Comment 2"));
    }
}
