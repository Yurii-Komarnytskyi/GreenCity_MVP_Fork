package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventCommentDtoResponse;
import greencity.dto.event.EventCommentRequestDto;
import greencity.dto.event.EventCommentResponseDto;
import greencity.dto.user.UserVO;
import greencity.service.EventCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing event comments. Provides endpoints for adding,
 * retrieving, and replying to comments.
 *
 * @author Viktoriia Rychenko
 */
@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
public class EventCommentController {
    private final EventCommentService eventCommentService;

    /**
     * Adds a comment to an event.
     *
     * @param eventId    the ID of the event
     * @param requestDto the request body containing the comment text
     * @param user       the currently authenticated user
     * @return the created comment
     */
    @Operation(summary = "Add an event comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = AddEventCommentDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping
    public ResponseEntity<AddEventCommentDtoResponse> addComment(
        @PathVariable Long eventId,
        @Valid @RequestBody EventCommentRequestDto requestDto,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventCommentService.addComment(eventId, user.getId(), requestDto));
    }

    /**
     * Counts the number of comments for a given event.
     *
     * @param eventId the ID of the event
     * @return the total number of comments
     */
    @Operation(summary = "Count event comments.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/count")
    public ResponseEntity<Long> countComments(@PathVariable Long eventId) {
        Long count = eventCommentService.countCommentsByEvent(eventId);
        return ResponseEntity.ok(count);
    }

    /**
     * Retrieves a comment by its ID.
     *
     * @param eventId   the ID of the event
     * @param commentId the ID of the comment
     * @return the comment details
     */
    @Operation(summary = "Get event comment by ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = EventCommentResponseDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<EventCommentResponseDto> getCommentById(
        @PathVariable Long eventId,
        @PathVariable Long commentId) {
        return ResponseEntity.ok(eventCommentService.getCommentById(eventId, commentId));
    }

    /**
     * Replies to a specific comment.
     *
     * @param commentId  the ID of the parent comment
     * @param requestDto the request body containing the reply text
     * @param user       the currently authenticated user
     * @return the created reply comment
     */
    @Operation(summary = "Reply to a comment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED,
            content = @Content(schema = @Schema(implementation = AddEventCommentDtoResponse.class))),
        @ApiResponse(responseCode = "400", description = HttpStatuses.BAD_REQUEST),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @PostMapping("/{commentId}/reply")
    public ResponseEntity<AddEventCommentDtoResponse> replyToComment(
        @PathVariable Long commentId,
        @RequestBody @Valid EventCommentRequestDto requestDto,
        @Parameter(hidden = true) @CurrentUser UserVO user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(eventCommentService.replyToComment(commentId, user.getId(), requestDto));
    }

    /**
     * Retrieves all comments for a specific event.
     *
     * @param eventId the ID of the event
     * @param page    the page number (default is 0)
     * @param size    the number of items per page (default is 10)
     * @return a pageable list of comments with pagination details
     */
    @Operation(summary = "Get all comments for an event.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK,
            content = @Content(schema = @Schema(implementation = PageableAdvancedDto.class))),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping
    public ResponseEntity<PageableAdvancedDto<EventCommentResponseDto>> getCommentsByEvent(
        @PathVariable Long eventId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        PageableAdvancedDto<EventCommentResponseDto> response =
            eventCommentService.getCommentsByEvent(eventId, page, size);
        return ResponseEntity.ok(response);
    }

}
