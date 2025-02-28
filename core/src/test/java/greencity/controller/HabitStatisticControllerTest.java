package greencity.controller;

import greencity.dto.habitstatistic.*;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.service.HabitStatisticService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HabitStatisticControllerTest {

    @Mock
    private HabitStatisticService habitStatisticService;

    @InjectMocks
    private HabitStatisticController habitStatisticController;

    @Test
    void findAllByHabitIdTest() {
        Long habitId = 1L;
        GetHabitStatisticDto mockResponse = new GetHabitStatisticDto();
        when(habitStatisticService.findAllStatsByHabitId(habitId)).thenReturn(mockResponse);

        ResponseEntity<GetHabitStatisticDto> response = habitStatisticController.findAllByHabitId(habitId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void findAllByHabitIdNotFoundTest() {
        Long habitId = 999L;
        when(habitStatisticService.findAllStatsByHabitId(habitId))
            .thenThrow(new NotFoundException("Habit not found by id: " + habitId));

        NotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
            NotFoundException.class,
            () -> habitStatisticController.findAllByHabitId(habitId));
        assertEquals("Habit not found by id: " + habitId, exception.getMessage());
    }

    @Test
    void findAllStatsByHabitAssignIdTest() {
        Long habitAssignId = 1L;
        List<HabitStatisticDto> mockResponse = Collections.singletonList(new HabitStatisticDto());
        when(habitStatisticService.findAllStatsByHabitAssignId(habitAssignId)).thenReturn(mockResponse);

        ResponseEntity<List<HabitStatisticDto>> response =
            habitStatisticController.findAllStatsByHabitAssignId(habitAssignId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void findAllStatsByHabitAssignIdNotFoundTest() {
        Long habitAssignId = 999L;
        when(habitStatisticService.findAllStatsByHabitAssignId(habitAssignId))
            .thenThrow(new NotFoundException("Habit assign not found"));

        NotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
            NotFoundException.class,
            () -> habitStatisticController.findAllStatsByHabitAssignId(habitAssignId));

        assertEquals("Habit assign not found", exception.getMessage());
    }

    @Test
    void saveHabitStatisticTest() {
        Long habitId = 1L;
        AddHabitStatisticDto inputDto = new AddHabitStatisticDto();
        HabitStatisticDto mockResponse = new HabitStatisticDto();
        UserVO userVO = UserVO.builder().id(1L).build();
        when(habitStatisticService.saveByHabitIdAndUserId(habitId, 1L, inputDto)).thenReturn(mockResponse);

        ResponseEntity<HabitStatisticDto> response =
            habitStatisticController.saveHabitStatistic(inputDto, userVO, habitId);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void saveHabitStatisticAlreadyExistsTest() {
        Long habitId = 1L;
        AddHabitStatisticDto inputDto = new AddHabitStatisticDto();
        UserVO userVO = UserVO.builder().id(1L).build();
        when(habitStatisticService.saveByHabitIdAndUserId(habitId, 1L, inputDto))
            .thenThrow(new NotSavedException("Habit statistic already exists with such date"));

        try {
            habitStatisticController.saveHabitStatistic(inputDto, userVO, habitId);
        } catch (NotSavedException e) {
            assertEquals("Habit statistic already exists with such date", e.getMessage());
        }
    }

    @Test
    void saveHabitStatisticNotFoundTest() {
        Long habitId = 1L;
        AddHabitStatisticDto inputDto = new AddHabitStatisticDto();
        UserVO userVO = UserVO.builder().id(1L).build();
        when(habitStatisticService.saveByHabitIdAndUserId(habitId, 1L, inputDto))
            .thenThrow(new NotFoundException("Habit assign not found with user id and habit id: 1, 1"));

        try {
            habitStatisticController.saveHabitStatistic(inputDto, userVO, habitId);
        } catch (NotFoundException e) {
            assertEquals("Habit assign not found with user id and habit id: 1, 1", e.getMessage());
        }
    }

    @Test
    void saveHabitStatisticInvalidDateTest() {
        Long habitId = 1L;
        AddHabitStatisticDto inputDto = new AddHabitStatisticDto();
        UserVO userVO = UserVO.builder().id(1L).build();
        when(habitStatisticService.saveByHabitIdAndUserId(habitId, 1L, inputDto))
            .thenThrow(new BadRequestException("Wrong date"));

        try {
            habitStatisticController.saveHabitStatistic(inputDto, userVO, habitId);
        } catch (BadRequestException e) {
            assertEquals("Wrong date", e.getMessage());
        }
    }

    @Test
    void updateStatisticTest() {
        Long id = 1L;
        UpdateHabitStatisticDto inputDto = new UpdateHabitStatisticDto();
        UpdateHabitStatisticDto mockResponse = new UpdateHabitStatisticDto();
        UserVO userVO = UserVO.builder().id(1L).build();
        when(habitStatisticService.update(id, 1L, inputDto)).thenReturn(mockResponse);

        ResponseEntity<UpdateHabitStatisticDto> response =
            habitStatisticController.updateStatistic(id, userVO, inputDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void updateStatisticNotFoundTest() {
        Long id = 999L;
        UpdateHabitStatisticDto inputDto = new UpdateHabitStatisticDto();
        UserVO userVO = UserVO.builder().id(1L).build();
        when(habitStatisticService.update(id, 1L, inputDto))
            .thenThrow(new NotFoundException("Statistic not found"));

        NotFoundException exception = org.junit.jupiter.api.Assertions.assertThrows(
            NotFoundException.class,
            () -> habitStatisticController.updateStatistic(id, userVO, inputDto));

        assertEquals("Statistic not found", exception.getMessage());
    }

    @Test
    void getTodayStatisticsForAllHabitItemsTest() {
        String language = "en";
        List<HabitItemsAmountStatisticDto> mockResponse = Collections.singletonList(new HabitItemsAmountStatisticDto());
        Locale locale = new Locale(language);
        when(habitStatisticService.getTodayStatisticsForAllHabitItems(language)).thenReturn(mockResponse);

        ResponseEntity<List<HabitItemsAmountStatisticDto>> response =
            habitStatisticController.getTodayStatisticsForAllHabitItems(locale);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void findAmountOfAcquiredHabitsTest() {
        Long userId = 1L;
        Long mockResponse = 10L;
        when(habitStatisticService.getAmountOfAcquiredHabitsByUserId(userId)).thenReturn(mockResponse);

        ResponseEntity<Long> response = habitStatisticController.findAmountOfAcquiredHabits(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    void findAmountOfHabitsInProgressTest() {
        Long userId = 1L;
        Long mockResponse = 5L;
        when(habitStatisticService.getAmountOfHabitsInProgressByUserId(userId)).thenReturn(mockResponse);

        ResponseEntity<Long> response = habitStatisticController.findAmountOfHabitsInProgress(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }
}
