package greencity.controller;

import greencity.annotations.ApiPageable;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.notification.NotificationRequestDto;
import greencity.dto.notification.NotificationResponseDto;
import greencity.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Method for creating a new Notification
     *
     * @param notificationRequestDto for Notification creation.
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Create a new notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping
    public ResponseEntity<NotificationResponseDto> addNotification(NotificationRequestDto notificationRequestDto) {

        NotificationResponseDto resp = notificationService.addNotification(notificationRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(resp);
    }

    /**
     * Method for getting a Notification by its id
     *
     * @param id for finding by id.
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Get a notification by its id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDto> getById(@PathVariable("id") Long id) {
        NotificationResponseDto notificationResponseDto = notificationService
            .getNotificationById(id);
        return ResponseEntity.ok().body(notificationResponseDto);
    }

    /**
     * Method for getting Notifications by their sender id
     *
     * @param id       for finding by sender id.
     * @param pageable
     *
     * @return {@link PageableDto<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Get notifications by their sender id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/sender/{id}")
    @ApiPageable
    public ResponseEntity<PageableDto<NotificationResponseDto>> getAllNotificationsBySender(@PathVariable("id") Long id,
        @Parameter(hidden = true) Pageable pageable) {
        Page<NotificationResponseDto> notificationResponseDtoPage =
            notificationService.getNotificationsBySender(id, pageable);
        PageableDto<NotificationResponseDto> notsBySender = new PageableDto<>(
            notificationResponseDtoPage.getContent(),
            notificationResponseDtoPage.getTotalElements(),
            notificationResponseDtoPage.getNumber(),
            notificationResponseDtoPage.getTotalPages());
        System.out.println();
        return ResponseEntity.ok().body(notsBySender);
    }

    /**
     * Method for getting Notifications by their receiver id
     *
     * @param id       for finding by receiver id.
     * @param pageable
     *
     * @return {@link PageableDto<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Get notifications by their receiver id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/receiver/{id}")
    @ApiPageable
    public ResponseEntity<PageableDto<NotificationResponseDto>> getAllNotificationsByReceiver(
        @PathVariable("id") Long id,
        @Parameter(hidden = true) Pageable pageable) {
        Page<NotificationResponseDto> notificationResponseDtoPage =
            notificationService.getNotificationsByReceiver(id, pageable);

        PageableDto<NotificationResponseDto> notsByReceiver = getPageableDtoObject(notificationResponseDtoPage);
        return ResponseEntity.ok().body(notsByReceiver);
    }

    /**
     * Method for getting Notifications by their viewed status
     *
     * @param isViewed for finding by viewed status.
     * @param pageable
     *
     * @return {@link PageableDto<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Get notifications by their viewed status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/viewed/{isViewed}")
    @ApiPageable
    public ResponseEntity<PageableDto<NotificationResponseDto>> getAllNotificationsByViewedStatus(
        @PathVariable("isViewed") Boolean isViewed,
        @Parameter(hidden = true) Pageable pageable) {
        Page<NotificationResponseDto> notificationResponseDtoPage =
            notificationService.getNotificationsByViewedStatus(isViewed, pageable);

        PageableDto<NotificationResponseDto> notsByViewedStatus = getPageableDtoObject(notificationResponseDtoPage);
        return ResponseEntity.ok().body(notsByViewedStatus);
    }

    /**
     * Method for getting Notifications by their section
     *
     * @param section  for finding by receiver id.
     * @param pageable
     *
     * @return {@link PageableDto<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Get notifications by their section")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/section/{isViewed}")
    @ApiPageable
    public ResponseEntity<PageableDto<NotificationResponseDto>> getAllNotificationsBySection(
        @PathVariable("section") String section,
        @Parameter(hidden = true) Pageable pageable) {
        Page<NotificationResponseDto> notificationResponseDtoPage =
            notificationService.getNotificationsBySection(section, pageable);

        PageableDto<NotificationResponseDto> notsByViewedStatus = getPageableDtoObject(notificationResponseDtoPage);
        return ResponseEntity.ok().body(notsByViewedStatus);
    }

    /**
     * Method for marking a bunch of Notifications by their receiver id as read
     *
     * @param receiverId for finding by receiver id.
     *
     * @return {@link String} with description of the result.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Mark all notifications as read by their receiver")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "503", description = HttpStatuses.INTERNAL_SERVER_ERROR)
    })
    @PostMapping("/{receiverId}/markAllRead")
    public ResponseEntity<String> markAllNotificationsAsRead(@PathVariable("receiverId") Long receiverId) {

        boolean areAllMarked = notificationService.markAllNotViewedAsRead(receiverId);
        return areAllMarked ? ResponseEntity.ok().body("All notifications are read")
            : ResponseEntity.internalServerError().body("An error occurred by viewing notifications");

    }

    /**
     * Method for marking a particular Notification by its id as read
     *
     * @param notificationId for finding by a notification by id.
     *
     * @return {@link String} with description of the result.
     * @author Mykhailo Derecha
     */
    @Operation(summary = "Mark notification as read by its id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN)
    })
    @PostMapping("/{notificationId}/markRead")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable("notificationId") Long notificationId) {
        boolean isMarked = notificationService.markAsRead(notificationId);
        return isMarked ? ResponseEntity.ok().body("A notification is read")
            : ResponseEntity.internalServerError().body("An error occurred by viewing notification");

    }

    /**
     * Private method for marking returning Page<NotificationResponseDto> as
     * PageableDto<NotificationResponseDto>
     *
     * @param notificationResponsePage for passing Page<NotificationResponseDto> we
     *                                 want to cast as PageableDto<>
     *
     * @return {@link PageableDto<NotificationResponseDto>}
     * @author Mykhailo Derecha
     */
    private PageableDto<NotificationResponseDto> getPageableDtoObject(
        Page<NotificationResponseDto> notificationResponsePage) {
        return new PageableDto<>(
            notificationResponsePage.getContent(),
            notificationResponsePage.getTotalElements(),
            notificationResponsePage.getNumber(),
            notificationResponsePage.getTotalPages());
    }

}
