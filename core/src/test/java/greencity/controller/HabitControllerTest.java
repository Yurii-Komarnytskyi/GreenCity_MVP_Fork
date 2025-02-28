package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.config.SecurityConfig;
import greencity.dto.PageableDto;
import greencity.dto.habit.AddCustomHabitDtoRequest;
import greencity.dto.habit.AddCustomHabitDtoResponse;
import greencity.dto.habit.HabitDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserVO;
import greencity.enums.ShoppingListItemStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.service.HabitService;
import greencity.service.TagsService;
import greencity.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.*;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    @Mock
    private HabitService habitService;

    @Mock
    private TagsService tagsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private HabitController habitController;

    private MockMvc mockMvc;

    private ObjectMapper mapper;

    private Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
        mapper = new ObjectMapper();
    }

    @Test
    void testGetHabitBiIdReturns200() throws Exception {

        Long id = 1L;
        Locale lang = Locale.ENGLISH;

        HabitDto habitResponseDto = new HabitDto();
        habitResponseDto.setId(id);
        habitResponseDto.setIsCustomHabit(true);
        habitResponseDto.setComplexity(2);
        habitResponseDto.setDefaultDuration(10);
        habitResponseDto.setAmountAcquiredUsers(33L);
        habitResponseDto.setTags(List.of("red", "green"));
        habitResponseDto.setImage("img.gif");

        given(habitService.getByIdAndLanguageCode(id, String.valueOf(lang))).willReturn(habitResponseDto);

        ResultActions response = mockMvc.perform(get("/habit/{id}", id).accept(MediaType.APPLICATION_JSON));

        // then - verify the output
        response.andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.id", is(habitResponseDto.getId().intValue())))
            .andExpect(jsonPath("$.isCustomHabit", is(habitResponseDto.getIsCustomHabit())))
            .andExpect(jsonPath("$.complexity", is(habitResponseDto.getComplexity())))
            .andExpect(jsonPath("$.defaultDuration", is(habitResponseDto.getDefaultDuration())))
            .andExpect(jsonPath("$.amountAcquiredUsers", is(habitResponseDto.getAmountAcquiredUsers().intValue())))
            .andExpect(jsonPath("$.tags", is(habitResponseDto.getTags())))
            .andExpect(jsonPath("$.image", is(habitResponseDto.getImage())));

    }

    @Test
    void testGetHabitBiIdReturns400() throws Exception {

        String id = "990L";
        Locale lang = Locale.ENGLISH;
        Mockito.when(habitService.getByIdAndLanguageCode(-1L, lang.getLanguage())).thenThrow(NotFoundException.class);

        mockMvc.perform(get("/habit/{id}", id))

            .andExpect(status().isBadRequest())
            .andDo(print());

    }

    @Test
    void testGetAllHabitsByLanguageCodeReturns200() throws Exception {

        UserVO userVO = getUserVO();

        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Locale lang = Locale.ENGLISH;

        List<HabitDto> habitList = List.of(new HabitDto()
            .setId(1L)
            .setIsCustomHabit(true)
            .setComplexity(2)
            .setDefaultDuration(10)
            .setAmountAcquiredUsers(33L)
            .setTags(List.of("red", "green"))
            .setImage("img.gif"),
            new HabitDto()
                .setId(3L)
                .setIsCustomHabit(false)
                .setComplexity(3)
                .setDefaultDuration(5)
                .setAmountAcquiredUsers(11L)
                .setTags(List.of("blue", "white"))
                .setImage("img_habit.gif"),
            new HabitDto()
                .setId(7L)
                .setIsCustomHabit(false)
                .setComplexity(1)
                .setDefaultDuration(11)
                .setAmountAcquiredUsers(22L)
                .setTags(List.of("yellow", "magenta", "pink"))
                .setImage("img_habit_new.gif")

        );

        PageableDto<HabitDto> habitDtoPageableDto =
            new PageableDto<>(habitList, habitList.size(), pageNumber, pageSize);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        given(habitService.getAllHabitsByLanguageCode(any(UserVO.class), any(Pageable.class), eq(lang.getLanguage())))
            .willReturn(habitDtoPageableDto);

        MvcResult result = mockMvc.perform(get("/habit")
            .principal(principal)
            .param("locale", lang.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.page[0].id").value(1))
            .andExpect(jsonPath("$.page[1].id").value(3))
            .andExpect(jsonPath("$.page[2].id").value(7))
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getAllHabitsByLanguageCode(any(UserVO.class),
            any(Pageable.class), eq(lang.getLanguage()));

    }

    @Test
    void testGetShoppingListItemsReturns200() throws Exception {

        Locale lang = Locale.ENGLISH;

        List<ShoppingListItemDto> shoppingListItems = List.of(new ShoppingListItemDto().setId(1L)
            .setText("shopping item 1")
            .setStatus("active"),

            new ShoppingListItemDto().setId(3L)
                .setText("shopping item 2")
                .setStatus("active")

        );

        given(habitService.getShoppingListForHabit(1L, lang.getLanguage())).willReturn(shoppingListItems);

        MvcResult result = mockMvc.perform(get("/habit/{id}/shopping-list", 1L)
            .principal(principal)
            .param("locale", lang.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[1].id").value(3))
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getShoppingListForHabit(eq(1L),
            eq(lang.getLanguage()));

    }

    @Test
    void testGetShoppingListItemsReturns400() throws Exception {

        String id = "990L";
        Locale lang = Locale.ENGLISH;
        Mockito.when(habitService.getShoppingListForHabit(-1L, lang.getLanguage()))
            .thenThrow(NotFoundException.class);

        mockMvc.perform(get("/habit/{id}/shopping-list", id))

            .andExpect(status().isBadRequest())
            .andDo(print());

    }

    @Test
    void testGetAllByTagsAndLanguageCodeReturns200() throws Exception {

        Locale lang = Locale.ENGLISH;
        List<String> tags = List.of("eco", "polluted", "free");

        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<HabitDto> habitList = List.of(new HabitDto()
            .setId(1L)
            .setIsCustomHabit(true)
            .setComplexity(2)
            .setDefaultDuration(10)
            .setAmountAcquiredUsers(33L)
            .setTags(List.of("red", "green"))
            .setImage("img.gif"),
            new HabitDto()
                .setId(3L)
                .setIsCustomHabit(false)
                .setComplexity(3)
                .setDefaultDuration(5)
                .setAmountAcquiredUsers(11L)
                .setTags(List.of("blue", "white"))
                .setImage("img_habit.gif"),
            new HabitDto()
                .setId(7L)
                .setIsCustomHabit(false)
                .setComplexity(1)
                .setDefaultDuration(11)
                .setAmountAcquiredUsers(22L)
                .setTags(List.of("yellow", "magenta", "pink"))
                .setImage("img_habit_new.gif")

        );

        PageableDto<HabitDto> habitDtoPageableDto =
            new PageableDto<>(habitList, habitList.size(), pageNumber, pageSize);

        given(habitService.getAllByTagsAndLanguageCode(any(Pageable.class), eq(tags), eq(lang.getLanguage())))
            .willReturn(habitDtoPageableDto);

        MvcResult result = mockMvc.perform(get("/habit/tags/search")
            .param("tags", tags.toArray(new String[0]))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.page[0].id").value(1))
            .andExpect(jsonPath("$.page[1].id").value(3))
            .andExpect(jsonPath("$.page[2].id").value(7))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getAllByTagsAndLanguageCode(any(Pageable.class),
            eq(tags), eq(lang.getLanguage()));

    }

    @Test
    void testGetAllByTagsAndLanguageCodeReturns400() throws Exception {

        MvcResult result = mockMvc.perform(get("/habit/tags/search"))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andReturn();
    }

    @Test
    void getAllByDifferentParametersReturns200() throws Exception {

        UserVO userVO = getUserVO();

        Optional<List<String>> tags = Optional.of(List.of("eco", "free"));
        Optional<Boolean> isCustomHabit = Optional.of(Boolean.TRUE);
        Optional<List<Integer>> complexities = Optional.of(List.of(1));
        Locale lang = Locale.ENGLISH;

        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        List<HabitDto> habitList = List.of(new HabitDto()
            .setId(1L)
            .setIsCustomHabit(true)
            .setComplexity(2)
            .setDefaultDuration(10)
            .setAmountAcquiredUsers(33L)
            .setTags(List.of("red", "green"))
            .setImage("img.gif"),
            new HabitDto()
                .setId(3L)
                .setIsCustomHabit(false)
                .setComplexity(3)
                .setDefaultDuration(5)
                .setAmountAcquiredUsers(11L)
                .setTags(List.of("blue", "white"))
                .setImage("img_habit.gif"),
            new HabitDto()
                .setId(7L)
                .setIsCustomHabit(false)
                .setComplexity(1)
                .setDefaultDuration(11)
                .setAmountAcquiredUsers(22L)
                .setTags(List.of("yellow", "magenta", "pink"))
                .setImage("img_habit_new.gif")

        );

        PageableDto<HabitDto> habitDtoPageableDto =
            new PageableDto<>(habitList, habitList.size(), pageNumber, pageSize);

        when(userService.findByEmail(anyString())).thenReturn(userVO);
        given(habitService.getAllByDifferentParameters(any(UserVO.class), any(Pageable.class), eq(tags),
            eq(isCustomHabit), eq(complexities), eq(lang.getLanguage())))
            .willReturn(habitDtoPageableDto);

        MvcResult result = mockMvc.perform(get("/habit/search?tags=eco&tags=free&isCustomHabit=true&complexities=1")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.page[0].id").value(1))
            .andExpect(jsonPath("$.page[1].id").value(3))
            .andExpect(jsonPath("$.page[2].id").value(7))
            .andExpect(status()
                .isOk())
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getAllByDifferentParameters(any(UserVO.class),
            any(Pageable.class), eq(tags), eq(isCustomHabit), eq(complexities), eq(lang.getLanguage()));

        given(habitService.getAllByDifferentParameters(any(UserVO.class), any(Pageable.class),
            eq(Optional.empty()),
            eq(isCustomHabit), eq(Optional.empty()), eq(lang.getLanguage())))
            .willReturn(habitDtoPageableDto);

        result = mockMvc.perform(get("/habit/search?isCustomHabit=true")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.page[0].id").value(1))
            .andExpect(jsonPath("$.page[1].id").value(3))
            .andExpect(jsonPath("$.page[2].id").value(7))
            .andExpect(status()
                .isOk())
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getAllByDifferentParameters(any(UserVO.class), any(Pageable.class),
            eq(Optional.empty()),
            eq(isCustomHabit), eq(Optional.empty()), eq(lang.getLanguage()));

        given(habitService.getAllByDifferentParameters(any(UserVO.class), any(Pageable.class),
            eq(tags),
            eq(Optional.empty()), eq(Optional.empty()), eq(lang.getLanguage())))
            .willReturn(habitDtoPageableDto);

        result = mockMvc.perform(get("/habit/search?tags=eco&tags=free")
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.page[0].id").value(1))
            .andExpect(jsonPath("$.page[1].id").value(3))
            .andExpect(jsonPath("$.page[2].id").value(7))
            .andExpect(status()
                .isOk())
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getAllByDifferentParameters(any(UserVO.class), any(Pageable.class),
            eq(tags),
            eq(Optional.empty()), eq(Optional.empty()), eq(lang.getLanguage()));

    }

    @Test
    void testAddCustomHabitReturns201() throws Exception {

        MockMultipartFile mockImageFile = new MockMultipartFile(
            "image",
            "habit-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "This is an image".getBytes());

        List<HabitTranslationDto> habitTranslationList = List.of(new HabitTranslationDto()
            .setLanguageCode("en")
            .setHabitItem("habitItem")
            .setName("translation")
            .setDescription("description"));

        List<CustomShoppingListItemResponseDto> customShoppingListItemResponseDtoList = List.of(
            new CustomShoppingListItemResponseDto()
                .setId(9L)
                .setStatus(ShoppingListItemStatus.ACTIVE)
                .setText("some item response dto"));

        AddCustomHabitDtoResponse addCustomHabitDtoResponse = new AddCustomHabitDtoResponse();
        addCustomHabitDtoResponse.setComplexity(1);
        addCustomHabitDtoResponse.setId(5L);
        addCustomHabitDtoResponse.setHabitTranslations(habitTranslationList);
        addCustomHabitDtoResponse.setCustomShoppingListItemDto(customShoppingListItemResponseDtoList);
        addCustomHabitDtoResponse.setDefaultDuration(99);
        addCustomHabitDtoResponse.setTagIds(Set.of(11L, 22L));
        addCustomHabitDtoResponse.setUserId(1L);
        addCustomHabitDtoResponse.setImage("newImage.gif");

        AddCustomHabitDtoRequest addCustomHabitDtoRequest = new AddCustomHabitDtoRequest();
        addCustomHabitDtoRequest.setCustomShoppingListItemDto(customShoppingListItemResponseDtoList)
            .setHabitTranslations(habitTranslationList)
            .setImage("newImage.gif")
            .setComplexity(1)
            .setTagIds(Set.of(11L, 22L))
            .setDefaultDuration(99);

        String addEcoNewsDtoRequestJson = mapper.writeValueAsString(addCustomHabitDtoRequest);

        MockMultipartFile metadataPart = new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            addEcoNewsDtoRequestJson.getBytes());

        given(habitService.addCustomHabit(addCustomHabitDtoRequest, mockImageFile, "test@gmail.com"))
            .willReturn(addCustomHabitDtoResponse);

        MvcResult result = mockMvc.perform(multipart("/habit/custom")
            .file(mockImageFile)
            .file(metadataPart)

            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).addCustomHabit(addCustomHabitDtoRequest,
            mockImageFile, "test@gmail.com");

    }

    @Test
    void testAddCustomHabitReturns400() throws Exception {

        MvcResult result = mockMvc.perform(multipart("/habit/custom")

            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andReturn();

    }

    @Test
    void testFindAllHabitsTagsReturns200() throws Exception {

        Locale lang = Locale.ENGLISH;

        List<String> habitTags = List.of("eco", "friendly", "activity");

        given(tagsService.findAllHabitsTags(eq(lang.getLanguage()))).willReturn(habitTags);

        MvcResult result = mockMvc.perform(get("/habit/tags")
            .param("locale", lang.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size()").value(3))
            .andExpect(jsonPath("$[0]").value("eco"))
            .andExpect(jsonPath("$[2]").value("activity"))
            .andExpect(status().isOk())
            .andDo(print())
            .andReturn();

        verify(tagsService, times(1)).findAllHabitsTags(eq(lang.getLanguage()));

    }

    @Test
    void testGetFriendsAssignedToHabitProfilePicturesReturns200() throws Exception {

        List<UserProfilePictureDto> userProfilePictureDtoList = List.of(
            new UserProfilePictureDto()
                .setId(15L)
                .setName("John")
                .setProfilePicturePath("/usr/local/john.jpeg"),
            new UserProfilePictureDto()
                .setId(25L)
                .setName("Mary")
                .setProfilePicturePath("/usr/local/mary.jpeg"));

        given(habitService.getFriendsAssignedToHabitProfilePictures(eq(11L), eq(null)))
            .willReturn(userProfilePictureDtoList);

        MvcResult result = mockMvc.perform(get("/habit/{habitId}/friends/profile-pictures", 11L)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.size()").value(2))
            .andExpect(jsonPath("$[0].name").value("John"))
            .andExpect(jsonPath("$[1].name").value("Mary"))
            .andDo(print())
            .andReturn();

        verify(habitService, times(1)).getFriendsAssignedToHabitProfilePictures(eq(11L),
            eq(null));

    }

}
