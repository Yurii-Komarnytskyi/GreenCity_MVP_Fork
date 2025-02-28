package greencity.controller;

import greencity.service.LanguageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
class LanguageControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private LanguageController languageController;

    private static final String LANGUAGE_PATH = "/language";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(languageController).build();
    }

    @Test
    @DisplayName("Should_Return_All_Language_Codes")
    void getAllLanguageCodes() throws Exception {
        List<String> languages = List.of("ua", "en", "fr");
        when(languageService.findAllLanguageCodes()).thenReturn(languages);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.get(LANGUAGE_PATH)
            .accept(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0]").value(languages.get(0)))
            .andExpect(jsonPath("$[1]").value(languages.get(1)))
            .andExpect(jsonPath("$[2]").value(languages.get(2)));

        verify(languageService, times(1)).findAllLanguageCodes();
    }

    @Test
    @DisplayName("Should_Return_Empty_List_When_No_Language_Codes_Present_In_DB")
    void getEmptyListOfLanguageCodes() throws Exception {
        when(languageService.findAllLanguageCodes()).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get(LANGUAGE_PATH)
                .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

        verify(languageService, times(1)).findAllLanguageCodes();
    }
}