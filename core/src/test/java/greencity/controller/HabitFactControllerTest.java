package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.PageableDto;
import greencity.dto.habitfact.HabitFactDtoResponse;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactUpdateDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.service.HabitFactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HabitFactControllerTest {

    @Mock
    Validator validator;

    @Mock
    ModelMapper modelMapper;

    @Mock
    private HabitFactService habitFactService;

    @InjectMocks
    private HabitFactController habitFactController;

    MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private final String HABIT_FACT_CONTROLLER_LINK = "/facts";

    private final Long RANDOM_ID = 1L;

    @BeforeEach
    void setUp() throws Exception {
        // mockMvc setup was shamelessly stolen from Yurii Feduniak, god bless U
        mockMvc = MockMvcBuilders.standaloneSetup(habitFactController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(validator)
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should_Return_Random_Fact_By_Habit_Id")
    void getRandomFactByHabitId() throws Exception {
        LanguageTranslationDTO response = ModelUtils.getLanguageTranslationDTO();
        Locale locale = Locale.ENGLISH;

        when(habitFactService.getRandomHabitFactByHabitIdAndLanguage(RANDOM_ID, locale.getLanguage()))
            .thenReturn(response);

        mockMvc.perform(get("/facts/random/%d".formatted(RANDOM_ID)).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(habitFactService, times(1))
            .getRandomHabitFactByHabitIdAndLanguage(RANDOM_ID, locale.getLanguage());
    }

    @Test
    @DisplayName("Should_Return_Habit_Fact_Of_The_Day")
    void getHabitFactOfTheDay() throws Exception {
        LanguageTranslationDTO languageTranslationDTO = ModelUtils.getLanguageTranslationDTO();
        when(habitFactService.getHabitFactOfTheDay(RANDOM_ID)).thenReturn(languageTranslationDTO);

        mockMvc.perform(get("/facts/dayFact/%d".formatted(RANDOM_ID)).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(languageTranslationDTO)));

        verify(habitFactService, times(1)).getHabitFactOfTheDay(RANDOM_ID);
    }

    @Test
    @DisplayName("Should_Return_All_LanguageTranslationDTOs")
    void getAll() throws Exception {
        Locale locale = Locale.ENGLISH;
        LanguageTranslationDTO langTranslationDTO = ModelUtils.getLanguageTranslationDTO();
        PageableDto<LanguageTranslationDTO> response = new PageableDto<>(List.of(langTranslationDTO), 0, 0, 0);

        when(habitFactService.getAllHabitFacts(any(Pageable.class), eq(locale.getLanguage()))).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(get(HABIT_FACT_CONTROLLER_LINK)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(habitFactService, times(1)).getAllHabitFacts(any(Pageable.class), eq(locale.getLanguage()));
    }

    @Test
    @DisplayName("Should_Save_HabitFactPostDto")
    void save() throws Exception {
        HabitFactPostDto fact = ModelUtils.getHabitFactPostDto();
        HabitFactVO habitFactVO = ModelUtils.getHabitFactVO();
        HabitFactDtoResponse response = Mockito.mock(HabitFactDtoResponse.class);

        when(habitFactService.save(fact)).thenReturn(habitFactVO);
        when(modelMapper.map(habitFactVO, HabitFactDtoResponse.class)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(post(HABIT_FACT_CONTROLLER_LINK)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(fact)));

        resultActions
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(habitFactService, times(1)).save(fact);
    }

    @Test
    @DisplayName("Should_Update_By_HabitFactUpdateDto_And_Id")
    void update() throws Exception {
        HabitFactUpdateDto habitFactUpdateDto = ModelUtils.getHabitFactUpdateDto();
        HabitFactVO updatedHabitFactVO = ModelUtils.getHabitFactVO();
        HabitFactPostDto response = ModelUtils.getHabitFactPostDto();

        when(habitFactService.update(habitFactUpdateDto, RANDOM_ID)).thenReturn(updatedHabitFactVO);
        when(modelMapper.map(updatedHabitFactVO, HabitFactPostDto.class)).thenReturn(response);

        ResultActions resultActions = mockMvc.perform(put("/facts/%d".formatted(RANDOM_ID))
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(habitFactUpdateDto)));

        resultActions
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json(objectMapper.writeValueAsString(response)));

        verify(habitFactService, times(1)).update(habitFactUpdateDto, RANDOM_ID);
    }

    @Test
    @DisplayName("Should_Delete_Habit_Fact_By_Id")
    void delete() throws Exception {
        when(habitFactService.delete(RANDOM_ID)).thenReturn(RANDOM_ID);

        ResultActions response = mockMvc.perform(MockMvcRequestBuilders.delete("/facts/%d".formatted(RANDOM_ID)));

        response.andExpect(status().isOk());
        verify(habitFactService, times(1)).delete(RANDOM_ID);
    }
}