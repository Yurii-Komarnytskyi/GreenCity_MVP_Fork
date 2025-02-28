package greencity.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import greencity.constant.ErrorMessage;
import greencity.converters.UserArgumentResolver;
import greencity.dto.habit.*;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.user.UserShoppingListItemAdvanceDto;
import greencity.dto.user.UserVO;
import greencity.enums.HabitAssignStatus;
import greencity.enums.ShoppingListItemStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.handler.CustomExceptionHandler;
import greencity.service.HabitAssignService;
import greencity.service.UserService;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static greencity.ModelUtils.getPrincipal;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
public class HabitAssignControllerTest {
    @Mock
    private HabitAssignService habitAssignService;
    @Mock
    private UserService userService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ErrorAttributes errorAttributes;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private HabitAssignController habitAssignController;

    private MockMvc mockMvc;
    private Principal principal = getPrincipal();
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();

        attributes.put("timestamp", "2025-01-13T10:00:00");
        attributes.put("trace", "Test stack trace");

        mockMvc = MockMvcBuilders.standaloneSetup(habitAssignController)
            .setControllerAdvice(new CustomExceptionHandler(errorAttributes, objectMapper))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void assignDefaultTest() throws Exception {
        long habitId = 1L;

        HabitAssignManagementDto expectedDto = new HabitAssignManagementDto();
        expectedDto.setId(7L);

        UserVO mockUser = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.assignDefaultHabitForUser(eq(habitId), any(UserVO.class)))
            .thenReturn(expectedDto);

        mockMvc.perform(post("/habit/assign/" + habitId)
            .principal(principal)
            .param("habitId", String.valueOf(habitId)))
            .andExpect(status().isCreated())
            .andExpect(result -> {
                String resultDtoString = result.getResponse().getContentAsString();
                XmlMapper mapper = new XmlMapper();
                mapper.registerModule(new JavaTimeModule());
                HabitAssignManagementDto actualDto = mapper.readValue(resultDtoString, HabitAssignManagementDto.class);
                Assertions.assertNotNull(actualDto);
                Assertions.assertEquals(7L, actualDto.getId());
                Assertions.assertEquals(expectedDto.getId(), actualDto.getId());
            });

        verify(habitAssignService, times(1)).assignDefaultHabitForUser(eq(habitId), any(UserVO.class));
    }

    @Test
    void assignCustomTest() throws Exception {
        long habitId = 1L;

        HabitAssignManagementDto expectedDto = new HabitAssignManagementDto();
        expectedDto.setId(7L);

        UserVO mockUser = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.assignCustomHabitForUser(eq(habitId), any(UserVO.class),
            any(HabitAssignCustomPropertiesDto.class)))
            .thenReturn(List.of(expectedDto));

        HabitAssignCustomPropertiesDto requestDto = new HabitAssignCustomPropertiesDto();

        mockMvc.perform(post("/habit/assign/" + habitId + "/custom")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(result -> {
                String resultDtoString = result.getResponse().getContentAsString();
                XmlMapper mapper = new XmlMapper();
                mapper.registerModule(new JavaTimeModule());
                List<HabitAssignManagementDto> resultDto =
                    mapper.readValue(resultDtoString, new TypeReference<List<HabitAssignManagementDto>>() {
                    });
                Assertions.assertNotNull(resultDto);
                Assertions.assertEquals(1, resultDto.size());
                Assertions.assertEquals(7L, resultDto.get(0).getId());
            });

        verify(habitAssignService, times(1))
            .assignCustomHabitForUser(eq(habitId), any(UserVO.class), any(HabitAssignCustomPropertiesDto.class));
    }

    @Test
    void updateHabitAssignDurationTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(mockUser);

        HabitAssignUserDurationDto expectedDto = new HabitAssignUserDurationDto();
        expectedDto.setDuration(22);
        expectedDto.setHabitAssignId(habitAssignId);
        expectedDto.setUserId(2L);
        expectedDto.setHabitId(3L);
        expectedDto.setStatus(HabitAssignStatus.REQUESTED);
        expectedDto.setWorkingDays(3);

        when(habitAssignService.updateUserHabitInfoDuration(eq(habitAssignId), any(Long.class), any(Integer.class)))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(put("/habit/assign/{habitAssignId}/update-habit-duration", habitAssignId)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .param("duration", String.valueOf(22)))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse);
        Assertions.assertEquals(expectedDto.getDuration(),
            JsonPath.parse(jsonResponse).read("$.duration", Integer.class));
        Assertions.assertEquals(expectedDto.getHabitAssignId(),
            JsonPath.parse(jsonResponse).read("$.habitAssignId", Long.class));
        Assertions.assertEquals(expectedDto.getUserId(), JsonPath.parse(jsonResponse).read("$.userId", Long.class));
        Assertions.assertEquals(expectedDto.getStatus(),
            HabitAssignStatus.valueOf(JsonPath.parse(jsonResponse).read("$.status", String.class)));
        Assertions.assertEquals(expectedDto.getWorkingDays(),
            JsonPath.parse(jsonResponse).read("$.workingDays", Integer.class));

        verify(habitAssignService, times(1)).updateUserHabitInfoDuration(eq(habitAssignId), any(Long.class),
            any(Integer.class));
    }

    @Test
    void getHabitAssignTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();

        Locale locale = Locale.ENGLISH;
        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(7L);
        expectedDto.setStatus(HabitAssignStatus.REQUESTED);
        expectedDto.setUserId(27L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, mockUser.getId(), locale.getLanguage()))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(get("/habit/assign/{habitAssignId}", habitAssignId)
            .principal(principal)
            .header("Accept-Language", locale.getLanguage())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse);
        Assertions.assertEquals(expectedDto.getId(), JsonPath.parse(jsonResponse).read("$.id", Long.class));
        Assertions.assertEquals(expectedDto.getStatus(),
            HabitAssignStatus.valueOf(JsonPath.parse(jsonResponse).read("$.status", String.class)));
        Assertions.assertEquals(expectedDto.getUserId(), JsonPath.parse(jsonResponse).read("$.userId", Long.class));

        verify(habitAssignService, times(1)).getByHabitAssignIdAndUserId(habitAssignId, mockUser.getId(),
            locale.getLanguage());
    }

    @Test
    void getCurrentUserHabitAssignsByIdAndAcquiredTest() throws Exception {
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(7L);
        expectedDto.setStatus(HabitAssignStatus.REQUESTED);
        expectedDto.setUserId(27L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.getAllHabitAssignsByUserIdAndStatusNotCancelled(mockUser.getId(), locale.getLanguage()))
            .thenReturn(List.of(expectedDto));

        MvcResult result = mockMvc.perform(get("/habit/assign/allForCurrentUser")
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .header("Accept-Language", locale.getLanguage()))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse);

        int listSize = JsonPath.parse(jsonResponse).read("$.length()", Integer.class);
        Assertions.assertEquals(1, listSize, "The list should contain only one element");

        Assertions.assertEquals(expectedDto.getId(), JsonPath.parse(jsonResponse).read("$[0].id", Long.class));
        Assertions.assertEquals(expectedDto.getStatus(),
            HabitAssignStatus.valueOf(JsonPath.parse(jsonResponse).read("$[0].status", String.class)));
        Assertions.assertEquals(expectedDto.getUserId(), JsonPath.parse(jsonResponse).read("$[0].userId", Long.class));

        verify(habitAssignService, times(1)).getAllHabitAssignsByUserIdAndStatusNotCancelled(mockUser.getId(),
            locale.getLanguage());
    }

    @Test
    void getUserShoppingAndCustomShoppingListsTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;

        UserShoppingAndCustomShoppingListsDto expectedDto = new UserShoppingAndCustomShoppingListsDto();
        CustomShoppingListItemResponseDto customShoppingListItemResponseDto = new CustomShoppingListItemResponseDto();
        customShoppingListItemResponseDto.setId(99L);
        expectedDto.setCustomShoppingListItemDto(List.of(customShoppingListItemResponseDto));

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.getUserShoppingAndCustomShoppingLists(mockUser.getId(), habitAssignId,
            locale.getLanguage()))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(get("/habit/assign/{habitAssignId}/allUserAndCustomList", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .header("Accept-Language", locale.getLanguage()))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");
        Assertions.assertEquals(expectedDto.getCustomShoppingListItemDto().getFirst().getId(),
            JsonPath.parse(jsonResponse).read("$.customShoppingListItemDto[0].id", Long.class),
            "ID of the first customShoppingListItemDto should match");

        verify(habitAssignService, times(1)).getUserShoppingAndCustomShoppingLists(mockUser.getId(), habitAssignId,
            locale.getLanguage());
    }

    @Test
    void updateUserAndCustomShoppingListsTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;
        UserShoppingAndCustomShoppingListsDto listsDto = new UserShoppingAndCustomShoppingListsDto();

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        doNothing().when(habitAssignService).fullUpdateUserAndCustomShoppingLists(mockUser.getId(), habitAssignId,
            listsDto, locale.getLanguage());

        mockMvc.perform(put("/habit/assign/{habitAssignId}/allUserAndCustomList", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(listsDto))
            .header("Accept-Language", locale.getLanguage()))
            .andExpect(status().isOk())
            .andReturn();

        verify(habitAssignService, times(1)).fullUpdateUserAndCustomShoppingLists(mockUser.getId(), habitAssignId,
            listsDto, locale.getLanguage());
    }

    @Test
    void getListOfUserAndCustomShoppingListsInProgressTest() throws Exception {
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;

        UserShoppingAndCustomShoppingListsDto expectedDto = new UserShoppingAndCustomShoppingListsDto();
        CustomShoppingListItemResponseDto customShoppingListItemResponseDto = new CustomShoppingListItemResponseDto();
        customShoppingListItemResponseDto.setId(88L);
        expectedDto.setCustomShoppingListItemDto(List.of(customShoppingListItemResponseDto));

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.getListOfUserAndCustomShoppingListsWithStatusInprogress(mockUser.getId(),
            locale.getLanguage()))
            .thenReturn(List.of(expectedDto));

        MvcResult result = mockMvc.perform(get("/habit/assign/allUserAndCustomShoppingListsInprogress")
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal)
            .header("Accept-Language", locale.getLanguage()))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        int listSize = JsonPath.parse(jsonResponse).read("$.length()", Integer.class);
        Assertions.assertEquals(1, listSize, "The list should contain only one element");

        Assertions.assertEquals(expectedDto.getCustomShoppingListItemDto().getFirst().getId(),
            JsonPath.parse(jsonResponse).read("$[0].customShoppingListItemDto[0].id", Long.class),
            "ID of the first customShoppingListItemDto should match");

        verify(habitAssignService, times(1)).getListOfUserAndCustomShoppingListsWithStatusInprogress(mockUser.getId(),
            locale.getLanguage());
    }

    @Test
    void getAllHabitAssignsByHabitIdAndAcquiredTest() throws Exception {
        long habitId = 1L;
        Locale locale = Locale.ENGLISH;

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(7L);
        expectedDto.setStatus(HabitAssignStatus.ACQUIRED);
        expectedDto.setUserId(27L);

        when(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId, locale.getLanguage()))
            .thenReturn(List.of(expectedDto));

        MvcResult result = mockMvc.perform(get("/habit/assign/{habitId}/all", habitId)
            .accept(MediaType.APPLICATION_JSON)
            .header("Accept-Language", locale.getLanguage()))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        int listSize = JsonPath.parse(jsonResponse).read("$.length()", Integer.class);
        Assertions.assertEquals(1, listSize, "The list should contain only one element");

        HabitAssignDto actualDto = JsonPath.parse(jsonResponse).read("$[0]", HabitAssignDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the first habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getUserId(), actualDto.getUserId(), "ID of the userId should match");

        verify(habitAssignService, times(1)).getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId,
            locale.getLanguage());
    }

    @Test
    void getHabitAssignByHabitIdTest() throws Exception {
        long habitId = 1L;
        Locale locale = Locale.ENGLISH;
        UserVO mockUser = getUserVO();

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(8L);
        expectedDto.setStatus(HabitAssignStatus.ACQUIRED);
        expectedDto.setUserId(28L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.findHabitAssignByUserIdAndHabitId(mockUser.getId(), habitId, locale.getLanguage()))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(get("/habit/assign/{habitId}/active", habitId)
            .accept(MediaType.APPLICATION_JSON)
            .header("Accept-Language", locale.getLanguage())
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitAssignDto actualDto = JsonPath.parse(jsonResponse).read("$", HabitAssignDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getUserId(), actualDto.getUserId(), "ID of the userId should match");

        verify(habitAssignService, times(1)).findHabitAssignByUserIdAndHabitId(mockUser.getId(), habitId,
            locale.getLanguage());
    }

    @Test
    void getUsersHabitByHabitAssignIdTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;

        HabitDto expectedDto = new HabitDto();
        expectedDto.setId(8L);
        expectedDto.setIsCustomHabit(true);
        expectedDto.setComplexity(77);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(
            habitAssignService.findHabitByUserIdAndHabitAssignId(mockUser.getId(), habitAssignId, locale.getLanguage()))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(get("/habit/assign/{habitAssignId}/more", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .header("Accept-Language", locale.getLanguage())
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitDto actualDto = JsonPath.parse(jsonResponse).read("$", HabitDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the habitDto should match");
        Assertions.assertEquals(expectedDto.getComplexity(), actualDto.getComplexity(),
            "Complexity of the habitDto should match");
        Assertions.assertEquals(expectedDto.getIsCustomHabit(), actualDto.getIsCustomHabit(),
            "isCustomHabit() should match");

        verify(habitAssignService, times(1)).findHabitByUserIdAndHabitAssignId(mockUser.getId(), habitAssignId,
            locale.getLanguage());
    }

    @Test
    void updateAssignByHabitIdTest() throws Exception {
        long habitAssignId = 1L;

        HabitAssignStatDto givenDto = new HabitAssignStatDto();
        givenDto.setStatus(HabitAssignStatus.INPROGRESS);

        HabitAssignManagementDto expectedDto = new HabitAssignManagementDto();
        expectedDto.setId(habitAssignId);
        expectedDto.setStatus(HabitAssignStatus.INPROGRESS);
        expectedDto.setWorkingDays(6);

        when(habitAssignService.updateStatusByHabitAssignId(habitAssignId, givenDto))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(patch("/habit/assign/{habitAssignId}", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(givenDto)))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitAssignManagementDto actualDto = JsonPath.parse(jsonResponse).read("$", HabitAssignManagementDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(),
            "ID of the habitAssignManagementDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignManagementDto should match");
        Assertions.assertEquals(expectedDto.getWorkingDays(), actualDto.getWorkingDays(),
            "WorkingDays of the habitAssignManagementDto should match");

        verify(habitAssignService, times(1)).updateStatusByHabitAssignId(habitAssignId, givenDto);
    }

    @Test
    void enrollHabitTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;
        LocalDate date = LocalDate.of(2025, 1, 10);
        String formattedDate = date.toString();

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(4L);
        expectedDto.setStatus(HabitAssignStatus.ACQUIRED);
        expectedDto.setUserId(24L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.enrollHabit(habitAssignId, mockUser.getId(), date, locale.getLanguage()))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc
            .perform(post("/habit/assign/{habitAssignId}/enroll/{date}", habitAssignId, formattedDate)
                .accept(MediaType.APPLICATION_JSON)
                .header("Accept-Language", locale.getLanguage())
                .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitAssignDto actualDto = JsonPath.parse(jsonResponse).read("$", HabitAssignDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getUserId(), actualDto.getUserId(),
            "UserId of the habitAssignDto should match");

        verify(habitAssignService, times(1)).enrollHabit(habitAssignId, mockUser.getId(), date, locale.getLanguage());
    }

    @Test
    void unenrollHabitTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        LocalDate date = LocalDate.of(2025, 1, 10);
        String formattedDate = date.toString();

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(2L);
        expectedDto.setStatus(HabitAssignStatus.ACQUIRED);
        expectedDto.setUserId(22L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.unenrollHabit(habitAssignId, mockUser.getId(), date))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc
            .perform(post("/habit/assign/{habitAssignId}/unenroll/{date}", habitAssignId, formattedDate)
                .accept(MediaType.APPLICATION_JSON)
                .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitAssignDto actualDto = JsonPath.parse(jsonResponse).read("$", HabitAssignDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getUserId(), actualDto.getUserId(),
            "UserId of the habitAssignDto should match");

        verify(habitAssignService, times(1)).unenrollHabit(habitAssignId, mockUser.getId(), date);
    }

    @Test
    void getInprogressHabitAssignOnDateTest() throws Exception {
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;
        LocalDate date = LocalDate.of(2025, 1, 10);
        String formattedDate = date.toString();

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(11L);
        expectedDto.setStatus(HabitAssignStatus.ACQUIRED);
        expectedDto.setUserId(211L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.findInprogressHabitAssignsOnDate(mockUser.getId(), date, locale.getLanguage()))
            .thenReturn(List.of(expectedDto));

        MvcResult result = mockMvc.perform(get("/habit/assign/active/{date}", formattedDate)
            .accept(MediaType.APPLICATION_JSON)
            .header("Accept-Language", locale.getLanguage())
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitAssignDto actualDto = JsonPath.parse(jsonResponse).read("$.[0]", HabitAssignDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getUserId(), actualDto.getUserId(),
            "UserId of the habitAssignDto should match");

        verify(habitAssignService, times(1)).findInprogressHabitAssignsOnDate(mockUser.getId(), date,
            locale.getLanguage());
    }

    @Test
    void getHabitAssignBetweenDatesTest() throws Exception {
        UserVO mockUser = getUserVO();
        Locale locale = Locale.ENGLISH;
        LocalDate date1 = LocalDate.of(2025, 1, 7);
        LocalDate date2 = LocalDate.of(2025, 1, 10);

        HabitsDateEnrollmentDto expectedDto = new HabitsDateEnrollmentDto();
        expectedDto.setEnrollDate(date1);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.findHabitAssignsBetweenDates(mockUser.getId(), date1, date2, locale.getLanguage()))
            .thenReturn(List.of(expectedDto));

        MvcResult result = mockMvc.perform(get("/habit/assign/activity/{from}/to/{to}", date1, date2)
            .accept(MediaType.APPLICATION_JSON)
            .header("Accept-Language", locale.getLanguage())
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        List<HabitsDateEnrollmentDto> actualDtos =
            objectMapper.readValue(jsonResponse, new TypeReference<List<HabitsDateEnrollmentDto>>() {
            });
        Assertions.assertEquals(date1, actualDtos.get(0).getEnrollDate());
        Assertions.assertEquals(1, actualDtos.size(), "Size of the result array should be 1");

        verify(habitAssignService, times(1)).findHabitAssignsBetweenDates(mockUser.getId(), date1, date2,
            locale.getLanguage());
    }

    @Test
    void cancelHabitAssignTest() throws Exception {
        long habitId = 1L;
        UserVO mockUser = getUserVO();

        HabitAssignDto expectedDto = new HabitAssignDto();
        expectedDto.setId(12L);
        expectedDto.setStatus(HabitAssignStatus.ACQUIRED);
        expectedDto.setUserId(212L);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(habitAssignService.cancelHabitAssign(habitId, mockUser.getId()))
            .thenReturn(expectedDto);

        MvcResult result = mockMvc.perform(patch("/habit/assign/cancel/{habitId}", habitId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Assertions.assertNotNull(jsonResponse, "JSON response should not be null");

        HabitAssignDto actualDto = JsonPath.parse(jsonResponse).read("$", HabitAssignDto.class);
        Assertions.assertNotNull(actualDto, "The result should not be null");

        Assertions.assertEquals(expectedDto.getId(), actualDto.getId(), "ID of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getStatus(), actualDto.getStatus(),
            "Status of the habitAssignDto should match");
        Assertions.assertEquals(expectedDto.getUserId(), actualDto.getUserId(),
            "UserId of the habitAssignDto should match");

        verify(habitAssignService, times(1)).cancelHabitAssign(habitId, mockUser.getId());
    }

    @Test
    void deleteHabitAssignTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        doNothing().when(habitAssignService).deleteHabitAssign(habitAssignId, mockUser.getId());

        mockMvc.perform(delete("/habit/assign/delete/{habitAssignId}", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        verify(habitAssignService, times(1)).deleteHabitAssign(habitAssignId, mockUser.getId());
    }

    @Test
    void notFoundDeleteHabitAssignTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        attributes.put("path", "/habit/assign/delete/" + habitAssignId);
        attributes.put("message", ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        doThrow(new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId))
            .when(habitAssignService).deleteHabitAssign(habitAssignId, mockUser.getId());
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(attributes);

        mockMvc.perform(delete("/habit/assign/delete/{habitAssignId}", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId))
            .andReturn();

        verify(habitAssignService, times(1)).deleteHabitAssign(habitAssignId, mockUser.getId());
    }

    @Test
    void updateShoppingListStatusTest() throws Exception {
        UpdateUserShoppingListDto givenDto = new UpdateUserShoppingListDto();
        givenDto.setHabitAssignId(2L);
        UserShoppingListItemAdvanceDto userShoppingListItemAdvanceDto = new UserShoppingListItemAdvanceDto();
        userShoppingListItemAdvanceDto.setId(7L);
        userShoppingListItemAdvanceDto.setStatus(ShoppingListItemStatus.ACTIVE);
        givenDto.setUserShoppingListAdvanceDto(List.of(userShoppingListItemAdvanceDto));
        givenDto.setUserShoppingListItemId(1L);

        mockMvc.perform(put("/habit/assign/saveShoppingListForHabitAssign")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(givenDto)))
            .andExpect(status().isOk())
            .andReturn();

        verify(habitAssignService, times(1)).updateUserShoppingListItem(givenDto);
    }

    @Test
    void updateProgressNotificationHasDisplayedTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        doNothing().when(habitAssignService).updateProgressNotificationHasDisplayed(habitAssignId, mockUser.getId());

        mockMvc.perform(put("/habit/assign/{habitAssignId}/updateProgressNotificationHasDisplayed", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isOk())
            .andReturn();

        verify(habitAssignService, times(1)).updateProgressNotificationHasDisplayed(habitAssignId, mockUser.getId());
    }

    @Test
    void notFoundUpdateProgressNotificationHasDisplayedTest() throws Exception {
        long habitAssignId = 1L;
        UserVO mockUser = getUserVO();
        attributes.put("path", "/habit/assign/1/updateProgressNotificationHasDisplayed");
        attributes.put("message", ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId);

        when(userService.findByEmail(anyString())).thenReturn(mockUser);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(attributes);
        doThrow(new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId))
            .when(habitAssignService).updateProgressNotificationHasDisplayed(habitAssignId, mockUser.getId());

        mockMvc.perform(put("/habit/assign/{habitAssignId}/updateProgressNotificationHasDisplayed", habitAssignId)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId))
            .andReturn();

        verify(habitAssignService, times(1)).updateProgressNotificationHasDisplayed(habitAssignId, mockUser.getId());
    }
}
