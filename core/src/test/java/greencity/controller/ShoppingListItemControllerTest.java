package greencity.controller;

import greencity.converters.UserArgumentResolver;
import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.enums.ShoppingListItemStatus;
import greencity.service.ShoppingListItemService;
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
import org.springframework.validation.Validator;

import java.security.Principal;
import java.util.*;

import static greencity.ModelUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@ExtendWith(MockitoExtension.class)
class ShoppingListItemControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ShoppingListItemService shoppingListItemService;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ShoppingListItemController shoppingListItemController;

    @Mock
    private Validator mockValidator;

    @Mock
    private Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(shoppingListItemController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void balkDeleteUserShoppingListItemTest() throws Exception {
        when(shoppingListItemService.deleteUserShoppingListItems("1,2"))
                .thenReturn(List.of(1L, 2L));

        mockMvc.perform(delete("/user/shopping-list-items/user-shopping-list-items")
                        .param("ids", "1,2")
                        .header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(shoppingListItemService, times(1)).deleteUserShoppingListItems("1,2");
    }

    @Test
    void updateUserShoppingListItemStatusWithLanguageParamTest() throws Exception {
        long userShoppingListItemId = 1L;
        String status = "DONE";
        String language = "en";

        UserVO mockUser = getUserVO();

        UserShoppingListItemResponseDto responseDto = new UserShoppingListItemResponseDto();
        responseDto.setId(userShoppingListItemId);
        responseDto.setText("Test item text");
        responseDto.setStatus(ShoppingListItemStatus.DONE);

        when(principal.getName()).thenReturn(mockUser.getEmail());

        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        when(shoppingListItemService.updateUserShoppingListItemStatus(
            userShoppingListItemId, mockUser.getId(), language, status))
            .thenReturn(List.of(responseDto));

        mockMvc.perform(
            patch("/user/shopping-list-items/{userShoppingListItemId}/status/{status}", userShoppingListItemId, status)
                .principal(principal)
                .header("Accept-Language", language)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print());

        verify(userService, times(1)).findByEmail(mockUser.getEmail());
        verify(shoppingListItemService, times(1))
            .updateUserShoppingListItemStatus(userShoppingListItemId, mockUser.getId(), language, status);
    }

    @Test
    void updateUserShoppingListItemStatus() throws Exception {
        long userShoppingListItemId = 1L;
        String status = "DONE";

        UserVO mockUser = getUserVO();

        UserShoppingListItemResponseDto responseDto = new UserShoppingListItemResponseDto();
        responseDto.setId(userShoppingListItemId);
        responseDto.setText("Test item text");
        responseDto.setStatus(ShoppingListItemStatus.DONE);

        when(principal.getName()).thenReturn(mockUser.getEmail());

        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        when(shoppingListItemService.updateUserShoppingListItemStatus(
            userShoppingListItemId, mockUser.getId(), "en", status))
            .thenReturn(List.of(responseDto));

        mockMvc.perform(
            patch("/user/shopping-list-items/{userShoppingListItemId}/status/{status}", userShoppingListItemId, status)
                .principal(principal)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print());

        verify(userService, times(1)).findByEmail(mockUser.getEmail());
        verify(shoppingListItemService, times(1))
            .updateUserShoppingListItemStatus(userShoppingListItemId, mockUser.getId(), "en", status);
    }

    @Test
    void saveUserShoppingListItemTest() throws Exception {
        long habitId = 1L;
        String language = "en";
        List<ShoppingListItemRequestDto> requestDtoList = List.of(new ShoppingListItemRequestDto());
        UserVO mockUser = getUserVO();

        UserShoppingListItemResponseDto responseDto = new UserShoppingListItemResponseDto();
        responseDto.setId(habitId);
        responseDto.setText("Test item text");
        responseDto.setStatus(ShoppingListItemStatus.DONE);

        List<UserShoppingListItemResponseDto> responseDtoList = List.of(responseDto);

        when(principal.getName()).thenReturn(mockUser.getEmail());

        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        when(shoppingListItemService.saveUserShoppingListItems(eq(mockUser.getId()), eq(habitId), eq(requestDtoList),
            eq(language)))
            .thenReturn(responseDtoList);

        mockMvc.perform(post("/user/shopping-list-items?habitId=" + habitId)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .content("[{}]")
            .header("Accept-Language", language))
            .andExpect(status().isCreated())
            .andDo(print());

        verify(shoppingListItemService, times(1))
            .saveUserShoppingListItems(eq(mockUser.getId()), eq(habitId), eq(requestDtoList), eq(language));
    }

    @Test
    void getUserShoppingListItemsWithLanguageParamTest() throws Exception {
        long habitId = 1L;
        String language = "en";

        UserVO mockUser = getUserVO();
        when(shoppingListItemService.getUserShoppingList(mockUser.getId(), habitId, language))
            .thenReturn(Collections.emptyList());

        when(principal.getName()).thenReturn(mockUser.getEmail());

        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(get("/user/shopping-list-items/habits/{habitId}/shopping-list", habitId)
            .principal(principal)
            .header("Accept-Language", language))
            .andExpect(status().isOk())
            .andDo(print());

        verify(shoppingListItemService, times(1))
            .getUserShoppingList(mockUser.getId(), habitId, language);
    }

    @Test
    void getUserShoppingListItemWithoutLanguageParamTest() throws Exception {
        long habitId = 1L;

        UserVO mockUser = getUserVO();
        when(shoppingListItemService.getUserShoppingList(mockUser.getId(), habitId, "en"))
            .thenReturn(Collections.emptyList());
        when(principal.getName()).thenReturn(mockUser.getEmail());
        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(get("/user/shopping-list-items/habits/{habitId}/shopping-list", habitId)
            .principal(principal))
            .andExpect(status().isOk())
            .andDo(print());

        verify(shoppingListItemService, times(1))
            .getUserShoppingList(mockUser.getId(), habitId, "en");
    }

    @Test
    void findAllByUserTest() throws Exception {
        when(shoppingListItemService.findInProgressByUserIdAndLanguageCode(1L, "en"))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/user/shopping-list-items/1/get-all-inprogress")
                        .param("lang", "en"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(shoppingListItemService, times(1)).findInProgressByUserIdAndLanguageCode(1L, "en");
    }

    @Test
    void deleteUserShoppingListItemTest() throws Exception {
        long habitId = 1L;
        long shoppingListItemId = 1L;
        UserVO mockUser = getUserVO();

        doNothing().when(shoppingListItemService)
            .deleteUserShoppingListItemByItemIdAndUserIdAndHabitId(shoppingListItemId, mockUser.getId(), habitId);

        when(principal.getName()).thenReturn(mockUser.getEmail());

        when(userService.findByEmail(mockUser.getEmail())).thenReturn(mockUser);

        mockMvc.perform(delete("/user/shopping-list-items")
            .principal(principal)
            .param("habitId", String.valueOf(habitId))
            .param("shoppingListItemId", String.valueOf(shoppingListItemId))
            .header("Accept-Language", "en"))
            .andExpect(status().isOk())
            .andDo(print());

        verify(shoppingListItemService, times(1))
            .deleteUserShoppingListItemByItemIdAndUserIdAndHabitId(shoppingListItemId, mockUser.getId(), habitId);
    }

}
