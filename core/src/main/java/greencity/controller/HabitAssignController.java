package greencity.controller;

import greencity.annotations.ApiLocale;
import greencity.annotations.CurrentUser;
import greencity.annotations.ValidLanguage;
import greencity.constant.AppConstant;
import greencity.constant.HttpStatuses;
import greencity.dto.habit.*;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarDto;
import greencity.dto.user.UserVO;
import greencity.service.HabitAssignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping("/habit/assign")
public class HabitAssignController {
    private final HabitAssignService habitAssignService;

    /**
     * Method which assigns habit for {@link UserVO} with default props.
     *
     * @param habitId {@link HabitVO} id.
     * @param userVO  {@link UserVO} instance.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Assign habit with default properties for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @PostMapping("/{habitId}")
    public ResponseEntity<HabitAssignManagementDto> assignDefault(@PathVariable Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(habitAssignService.assignDefaultHabitForUser(habitId, userVO));
    }

    /**
     * Method which assigns habit for {@link UserVO} with custom props.
     *
     * @param habitId                        {@link HabitVO} id.
     * @param userVO                         {@link UserVO} instance.
     * @param habitAssignCustomPropertiesDto {@link HabitAssignCustomPropertiesDto}
     *                                       instance.
     * @return {@link ResponseEntity}.
     */
    @Operation(summary = "Assign habit with custom properties for current user and his friends.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @PostMapping("/{habitId}/custom")
    public ResponseEntity<List<HabitAssignManagementDto>> assignCustom(@PathVariable Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Valid @RequestBody HabitAssignCustomPropertiesDto habitAssignCustomPropertiesDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(habitAssignService.assignCustomHabitForUser(habitId, userVO, habitAssignCustomPropertiesDto));
    }

    /**
     * Method which updates duration of habit assigned for user.
     *
     * @param habitAssignId {@link HabitVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param duration      {@link Integer} with needed duration.
     * @return {@link ResponseEntity}.
     */
    @PutMapping("/{habitAssignId}/update-habit-duration")
    @Operation(summary = "Update duration of habit with habitAssignId for user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignUserDurationDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    public ResponseEntity<HabitAssignUserDurationDto> updateHabitAssignDuration(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @RequestParam @Min(AppConstant.MIN_DAYS_DURATION) @Max(AppConstant.MAX_DAYS_DURATION) Integer duration) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.updateUserHabitInfoDuration(habitAssignId, userVO.getId(), duration));
    }

    /**
     * Method returns {@link HabitAssignDto} by it's id, current user id and
     * specific language.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO}.
     * @param locale        needed language responseCode.
     * @return {@link HabitAssignDto}.
     */
    @Operation(summary = "Get habit assign.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/{habitAssignId}")
    public ResponseEntity<HabitAssignDto> getHabitAssign(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO, @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.getByHabitAssignIdAndUserId(habitAssignId, userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method for finding all inprogress, acquired {@link HabitAssignDto}'s for
     * current user.
     *
     * @param userVO {@link UserVO} instance.
     * @param locale needed language responseCode.
     * @return list of {@link HabitAssignDto}.
     */
    @Operation(summary = "Get (inprogress, acquired) assigned habits for current user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HabitAssignDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @ApiLocale
    @GetMapping("/allForCurrentUser")
    public ResponseEntity<List<HabitAssignDto>> getCurrentUserHabitAssignsByIdAndAcquired(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getAllHabitAssignsByUserIdAndStatusNotCancelled(userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method that return UserShoppingList and CustomShoppingList.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param locale        needed language responseCode.
     * @return User Shopping List and Custom Shopping List.
     */
    @Operation(summary = "Get user shopping and custom shopping lists by habitAssignId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserShoppingAndCustomShoppingListsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("{habitAssignId}/allUserAndCustomList")
    public ResponseEntity<UserShoppingAndCustomShoppingListsDto> getUserShoppingAndCustomShoppingLists(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getUserShoppingAndCustomShoppingLists(userVO.getId(), habitAssignId, locale.getLanguage()));
    }

    /**
     * Method that update UserShoppingList and CustomShopping List.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} instance.
     * @param locale        needed language responseCode.
     * @param listsDto      {@link UserShoppingAndCustomShoppingListsDto} instance.
     */
    @Operation(summary = "Update user and custom shopping lists",
        description = """
            If the item is already present in the db, the method updates it
            If item is not present in the db and id is null, the method attempts to add it to the user
            If some items from db are not present in the lists, the method deletes
            them (except for items with DISABLED status).
            """)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @PutMapping("{habitAssignId}/allUserAndCustomList")
    public ResponseEntity<ResponseEntity.BodyBuilder> updateUserAndCustomShoppingLists(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @Parameter(hidden = true) @ValidLanguage Locale locale,
        @Valid @RequestBody UserShoppingAndCustomShoppingListsDto listsDto) {
        habitAssignService.fullUpdateUserAndCustomShoppingLists(userVO.getId(), habitAssignId, listsDto,
            locale.getLanguage());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method that return list of UserShoppingLists and CustomShoppingLists for
     * current user, specific language and INPROGRESS status.
     *
     * @param userVO {@link UserVO} instance.
     * @param locale needed language responseCode.
     * @return List of User Shopping Lists and Custom Shopping Lists.
     */
    @Operation(summary = "Get list of user shopping list items and custom shopping list items with status INPROGRESS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = UserShoppingAndCustomShoppingListsDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/allUserAndCustomShoppingListsInprogress")
    public ResponseEntity<List<UserShoppingAndCustomShoppingListsDto>> getListOfUserAndCustomShoppingListsInprogress(
        @Parameter(hidden = true) @CurrentUser UserVO userVO, @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .getListOfUserAndCustomShoppingListsWithStatusInprogress(userVO.getId(), locale.getLanguage()));
    }

    /**
     * Method to return all inprogress, acquired {@link HabitAssignDto} by it's
     * {@link HabitVO} id.
     *
     * @param habitId {@link HabitVO} id.
     * @param locale  needed language responseCode.
     * @return {@link List} of {@link HabitAssignDto}.
     */
    @Operation(summary = "Get all inprogress, acquired assigns by certain habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = HabitAssignDto.class)))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @ApiLocale
    @GetMapping("/{habitId}/all")
    public ResponseEntity<List<HabitAssignDto>> getAllHabitAssignsByHabitIdAndAcquired(@PathVariable Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.getAllHabitAssignsByHabitIdAndStatusNotCancelled(habitId,
                locale.getLanguage()));
    }

    /**
     * Method to return {@link HabitAssignVO} by it's {@link HabitVO} id.
     *
     * @param habitId {@link HabitVO} id.
     * @param userVO  {@link UserVO} user.
     * @param locale  needed language responseCode.
     * @return {@link HabitAssignDto} instance.
     */
    @Operation(summary = "Get inprogress or acquired assign by habit id for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/{habitId}/active")
    public ResponseEntity<HabitAssignDto> getHabitAssignByHabitId(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long habitId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findHabitAssignByUserIdAndHabitId(userVO.getId(), habitId, locale.getLanguage()));
    }

    /**
     * Method to return {@link HabitDto} with more it's information by
     * {@link HabitAssignVO} id.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO} user.
     * @param locale        needed language responseCode.
     * @return {@link HabitDto} instance.
     */
    @Operation(summary = "Get habit assign by habit assign id for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @GetMapping("/{habitAssignId}/more")
    public ResponseEntity<HabitDto> getUsersHabitByHabitAssignId(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findHabitByUserIdAndHabitAssignId(userVO.getId(), habitAssignId, locale.getLanguage()));
    }

    /**
     * Method to update inprogress, acquired {@link HabitAssignVO} for it's
     * {@link HabitVO} id and current user.
     *
     * @param habitAssignId      {@link HabitAssignVO} id.
     * @param habitAssignStatDto {@link HabitAssignStatDto} instance.
     * @return {@link HabitAssignManagementDto}.
     */
    @Operation(summary = "Update inprogress, acquired user habit assign acquired or cancelled status.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/{habitAssignId}")
    public ResponseEntity<HabitAssignManagementDto> updateAssignByHabitId(
        @PathVariable Long habitAssignId, @Valid @RequestBody HabitAssignStatDto habitAssignStatDto) {
        return ResponseEntity.status(HttpStatus.OK).body(habitAssignService
            .updateStatusByHabitAssignId(habitAssignId, habitAssignStatDto));
    }

    /**
     * Method to enroll {@link HabitAssignVO} for current date.
     *
     * @param habitAssignId - id of {@link HabitAssignVO}.
     * @param userVO        {@link UserVO} user.
     * @param date          - {@link LocalDate} we want to enroll.
     * @param locale        - needed language responseCode.
     * @return {@link HabitStatusCalendarDto}.
     */
    @Operation(summary = "Enroll habit assign by habitAssignId for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @ApiLocale
    @PostMapping("/{habitAssignId}/enroll/{date}")
    public ResponseEntity<HabitAssignDto> enrollHabit(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.enrollHabit(habitAssignId, userVO.getId(), date, locale.getLanguage()));
    }

    /**
     * Method to unenroll {@link HabitAssignVO} for defined date.
     *
     * @param habitAssignId - id of {@link HabitAssignVO}.
     * @param userVO        {@link UserVO} user.
     * @param date          - {@link LocalDate} we want to unenroll.
     * @return {@link HabitAssignDto} instance.
     */
    @Operation(summary = "Unenroll assigned habit for a specific day.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{habitAssignId}/unenroll/{date}")
    public ResponseEntity<HabitAssignDto> unenrollHabit(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService.unenrollHabit(habitAssignId, userVO.getId(), date));
    }

    /**
     * Method to find all inprogress {@link HabitAssignVO} on certain
     * {@link LocalDate}.
     *
     * @param userVO {@link UserVO} user.
     * @param date   {@link LocalDate} date to check if has inprogress assigns.
     * @param locale needed language code.
     * @return {@link HabitAssignDto} instance.
     */
    @Operation(summary = "Get inprogress user habit assigns on certain date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @ApiLocale
    @GetMapping("/active/{date}")
    public ResponseEntity<List<HabitAssignDto>> getInprogressHabitAssignOnDate(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findInprogressHabitAssignsOnDate(userVO.getId(), date, locale.getLanguage()));
    }

    /**
     * Method to find all user inprogress activities {@link HabitsDateEnrollmentDto}
     * between the specified {@link LocalDate}s.
     *
     * @param userVO {@link UserVO} user.
     * @param from   The start {@link LocalDate} to retrieve from
     * @param to     The end {@link LocalDate} to retrieve to
     * @param locale needed language code.
     * @return {@link HabitsDateEnrollmentDto} instance.
     */
    @Operation(summary = "Get user inprogress activities between the specified dates.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitsDateEnrollmentDto.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED)
    })
    @ApiLocale
    @GetMapping("/activity/{from}/to/{to}")
    public ResponseEntity<List<HabitsDateEnrollmentDto>> getHabitAssignBetweenDates(
        @Parameter(hidden = true) @CurrentUser UserVO userVO,
        @PathVariable(value = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @PathVariable(value = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @Parameter(hidden = true) @ValidLanguage Locale locale) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(habitAssignService
                .findHabitAssignsBetweenDates(userVO.getId(), from, to, locale.getLanguage()));
    }

    /**
     * Method to cancel inprogress {@link HabitAssignVO} by it's {@link HabitVO} id
     * and current user id.
     *
     * @param habitId - id of {@link HabitVO}.
     * @param userVO  - {@link UserVO} user.
     * @return {@link HabitAssignDto}.
     */
    @Operation(summary = "Cancel inprogress user assigned habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = HabitAssignDto.class))),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PatchMapping("/cancel/{habitId}")
    public ResponseEntity<HabitAssignDto> cancelHabitAssign(@PathVariable Long habitId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return ResponseEntity.status(HttpStatus.OK).body(habitAssignService.cancelHabitAssign(habitId, userVO.getId()));
    }

    /**
     * Method delete habit assign {@link HabitAssignVO} for current {@link UserVO}
     * by habitAssignId.
     *
     * @param habitAssignId - id of {@link HabitAssignVO}.
     * @param userVO        - {@link UserVO} user.
     */
    @Operation(summary = "Delete habit assign by habitAssignId for current user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete/{habitAssignId}")
    public ResponseEntity<ResponseEntity.BodyBuilder> deleteHabitAssign(@PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        habitAssignService.deleteHabitAssign(habitAssignId, userVO.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method updates user shopping list item {@link UpdateUserShoppingListDto}.
     *
     * @param updateUserShoppingListDto - id of {@link UpdateUserShoppingListDto}.
     */
    @Operation(summary = "Update shopping list status for current habit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("/saveShoppingListForHabitAssign")
    public ResponseEntity<ResponseEntity.BodyBuilder> updateShoppingListStatus(
        @RequestBody UpdateUserShoppingListDto updateUserShoppingListDto) {
        habitAssignService.updateUserShoppingListItem(updateUserShoppingListDto);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * Method updates value progressNotificationHasDisplayed in HabitAssign to true
     * {@link Boolean}.
     *
     * @param habitAssignId {@link HabitAssignVO} id.
     * @param userVO        {@link UserVO}.
     */
    @Operation(summary = "Update value progressNotificationHasDisplayed to true.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PutMapping("{habitAssignId}/updateProgressNotificationHasDisplayed")
    public ResponseEntity<ResponseEntity.BodyBuilder> updateProgressNotificationHasDisplayed(
        @PathVariable Long habitAssignId,
        @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        habitAssignService.updateProgressNotificationHasDisplayed(habitAssignId, userVO.getId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}