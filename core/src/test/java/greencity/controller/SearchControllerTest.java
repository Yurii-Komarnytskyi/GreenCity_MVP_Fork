package greencity.controller;

import greencity.dto.PageableDto;
import greencity.dto.search.SearchNewsDto;
import greencity.dto.search.SearchResponseDto;
import greencity.service.LanguageService;
import greencity.service.SearchService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @Mock
    private LanguageService languageService;

    @Mock
    private LanguageController languageController;

    @InjectMocks
    private SearchController searchController;
    @Mock
    private Validator validator;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(searchController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(validator)
            .build();
    }

    @Test
    void searchEverythingTest() throws Exception {
        String searchQuery = "Title";
        Locale locale = Locale.ENGLISH;

        log.info("Chosen locale is {}", locale);

        SearchResponseDto expectedResponse = SearchResponseDto.builder()
            .ecoNews(List.of())
            .countOfResults(0L)
            .build();

        List<String> codes = languageService.findAllLanguageCodes();
        log.info("Found codes {}", codes);

        when(searchService.search(searchQuery, locale.getLanguage())).thenReturn(expectedResponse);

        mockMvc.perform(get("/search")
            .param("searchQuery", searchQuery)
            .param("locale", locale.getLanguage())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(searchService).search(searchQuery, locale.getLanguage());
    }

    @Test
    void searchEcoNewsTest() throws Exception {
        String searchQuery = "Eco news title";
        Locale locale = Locale.ENGLISH;
        PageableDto<SearchNewsDto> expectedResponse = new PageableDto<>(List.of(), 0, 0, 1);

        when(searchService.searchAllNews(any(Pageable.class), eq(searchQuery), eq(locale.getLanguage())))
            .thenReturn(expectedResponse);

        mockMvc.perform(get("/search/econews")
            .param("searchQuery", searchQuery)
            .param("locale", locale.getLanguage())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(searchService).searchAllNews(any(Pageable.class), eq(searchQuery), eq(locale.getLanguage()));
    }

}