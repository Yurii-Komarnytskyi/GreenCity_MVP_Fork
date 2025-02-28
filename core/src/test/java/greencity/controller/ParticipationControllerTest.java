package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.converters.UserArgumentResolver;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.ParticipationService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ParticipationControllerTest {
    @Mock
    ParticipationService participationService;

    @Mock
    UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    ParticipationController participationController;

    @Mock
    private ErrorAttributes errorAttributes;

    private MockMvc mockMvc;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();

        attributes.put("timestamp", "2025-01-13T10:00:00");
        attributes.put("trace", "Test stack trace");

        mockMvc = MockMvcBuilders.standaloneSetup(participationController)
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void cancelParticipationTest() throws Exception {
        doNothing().when(participationService).removeParticipation(anyLong(), anyLong());
        when(userService.findIdByEmail(anyString())).thenReturn(1L);

        mockMvc.perform(delete("/participation/1")
            .principal(() -> "testuser@gmail.com"))
            .andExpect(status().isOk())
            .andExpect(content().string("Participation successfully deleted"));
    }

    @Test
    void cancelParticipationInThePastTest() throws Exception {
        attributes.put("path", "/participation/1");
        attributes.put("message", "You cannot remove the participation from the event that is in the past");

        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(attributes);

        doThrow(new BadRequestException("You cannot remove the participation from the event that is in the past"))
            .when(participationService).removeParticipation(anyLong(), anyLong());
        when(userService.findIdByEmail(anyString())).thenReturn(1L);

        mockMvc.perform(delete("/participation/1")
            .principal(() -> "testuser@gmail.com"))
            .andExpect(status().isBadRequest());
    }
}
