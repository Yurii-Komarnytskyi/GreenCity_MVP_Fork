package greencity.controller;

import greencity.annotations.CurrentUser;
import greencity.constant.HttpStatuses;
import greencity.dto.PageableDto;
import greencity.dto.friendship.*;
import greencity.dto.user.UserVO;
import greencity.exception.exceptions.WrongIdException;
import greencity.service.FriendPageService;
import greencity.service.FriendshipService;
import greencity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final FriendPageService friendPageService;

    @Autowired
    public FriendshipController(
            FriendshipService friendshipService,
            FriendPageService friendPageService,
            UserService userService) {
        this.friendshipService = friendshipService;
        this.friendPageService = friendPageService;
    }

    /**
     * Handles the sending of a friendship request from the current user to the specified recipient.
     * This method creates a friendship request for the user represented by {@code userVO}
     * to the user identified by {@code recipientId}. If the request is successfully sent,
     * it returns a response indicating success with a status of {@code 201 Created}.
     * If the request cannot be sent (either because it already exists or for any other reason),
     * it returns a response indicating the failure with a status of {@code 406 Not Acceptable}.
     *
     * @param recipientId the ID of the user to whom the friendship request is being sent.
     *                    Must not be null. A {@link NotNull} validation ensures this.
     * @param userVO     the current user initiating the friendship request.
     *                   This is injected and hidden from the documentation.
     * @return a {@link ResponseEntity} containing {@link FriendshipResponseDto} with the outcome
     *         message and status of the request.
     */
    @Operation(summary = "Request friendship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    @PostMapping("/request/{recipientId}/")
    public ResponseEntity<FriendshipResponseDto> requestFriendship(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("recipientId") @NotNull(message = "User ID must not be null.") Long recipientId
            ) {
        boolean isRequested = friendshipService.requestFriendshipByUserId(userVO.getId(), recipientId);

        return (isRequested)
            ? ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new FriendshipResponseDto(true, "Friendship request sent successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship request already exists or cannot be sent."));
    }



    @Operation(summary = "Accept friendship request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })

    /**
     * Accepts a friendship request from the specified sender for the current user.
     * This method enables the authenticated user (represented by {@code userVO})
     * to accept a friendship request from another user whose ID is specified by {@code senderId}.
     * If the friendship request is accepted successfully, the method returns a response with status
     * {@code 200 OK} along with a success message. If the acceptance fails (for example, if the
     * friendship record does not exist or is not in the REQUESTED state), it returns a response
     * with status {@code 406 Not Acceptable} along with an error message explaining the cause
     * of the failure.
     *
     * @param userVO      the current authenticated user who is accepting the friendship request.
     *                    This parameter is injected and hidden from the API documentation.
     * @param senderId    the ID of the user whose friendship request is being accepted.
     *                    Must not be null, as enforced by the {@link NotNull} validation.
     * @return a {@link ResponseEntity} containing a {@link FriendshipResponseDto}
     *         with the result of the acceptance operation and the appropriate HTTP status.
     */
    @PutMapping("/accept/{senderId}/")
    public ResponseEntity<FriendshipResponseDto> acceptFriendship(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("senderId") @NotNull(message = "User ID must not be null.") Long senderId) {
        boolean isAccepted = friendshipService.acceptFriendshipRequestByUserId(senderId, userVO.getId());
        return (isAccepted)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship accepted successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship record must exist and be in REQUESTED state"));
    }

    /**
     * Cancels a friendship request previously sent to the specified recipient by the current user.
     * This method allows the authenticated user (represented by {@code userVO})
     * to cancel a friendship request that was sent to another user identified by {@code recipientId}.
     * If the cancellation is successful, it returns a response with status {@code 200 OK}
     * along with a success message. If the cancellation fails (for example, if there was no
     * preceding friendship request to cancel), it returns a response with status
     * {@code 406 Not Acceptable} and an error message indicating that the cancellation has failed.
     *
     * @param userVO      the current authenticated user who is cancelling the friendship request.
     *                    This parameter is injected and hidden from the API documentation.
     * @param recipientId the ID of the user to whom the friendship request was sent and is being cancelled.
     *                    Must not be null, as enforced by the {@link NotNull} validation.
     * @return a {@link ResponseEntity} containing a {@link FriendshipResponseDto}
     *         with the result of the cancellation operation and the appropriate HTTP status.
     */
    @Operation(summary = "Cancel friendship request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    @PutMapping("/cancel/{recipientId}/")
    public ResponseEntity<FriendshipResponseDto> cancelFriendshipRequest(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("recipientId") @NotNull(message = "User ID must not be null.") Long recipientId) {
        boolean isCancelled  = friendshipService.cancelFriendshipRequestByUserId(userVO.getId(), recipientId);
        return (isCancelled)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship request cancelled successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship request cancellation has failed."));
    }

    /**
     * Deletes an existing friendship between the current user and a specified friend.
     * This method allows the authenticated user (represented by {@code userVO})
     * to delete a friendship with another user identified by {@code friendId}.
     * If the friendship is successfully deleted, the method returns a response with status
     * {@code 200 OK} along with a success message. If the deletion fails (for example, because
     * the friendship record does not exist or is not in an appropriate state to delete),
     * it returns a response with status {@code 404 Not Found} and an error message indicating
     * that the deletion could not be performed.
     *
     * @param userVO   the current authenticated user who is initiating the deletion of the friendship.
     *                 This parameter is injected and hidden from the API documentation.
     * @param friendId the ID of the friend with whom the friendship is to be deleted.
     *                 Must not be null, as enforced by the {@link NotNull} validation.
     * @return a {@link ResponseEntity} containing a {@link FriendshipResponseDto}
     *         indicating the result of the deletion operation and the appropriate HTTP status.
     */
    @Operation(summary = "Delete friendship record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/delete/{friendId}/")
    public ResponseEntity<FriendshipResponseDto> deleteFriendship(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("friendId") @NotNull(message = "User ID must not be null.") Long friendId) {
        boolean isDeleted = friendshipService.deleteFriendByUserId(userVO.getId(), friendId);
        return  (isDeleted)
                 ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship deleted successfully."))
                 : ResponseEntity
                       .status(HttpStatus.NOT_FOUND)
                       .body(new FriendshipResponseDto(false, "Friendship record must exist and be in REQUESTED state"));
    }

    /**
     * Blocks friendship requests from a specified user for the current user.
     * This method allows the authenticated user (represented by {@code userVO})
     * to block all incoming friendship requests from another user identified by {@code senderId}.
     * If the operation is successful, it returns a response with status {@code 200 OK}
     * along with a success message. If the blocking operation fails (such as if the
     * friendship state is not suitable for blocking), the method returns a response
     * with status {@code 406 Not Acceptable} and an error message indicating failure.
     *
     * @param userVO   the current authenticated user who is executing the block operation.
     *                 This parameter is injected and hidden from API documentation.
     * @param senderId the ID of the sender from whom friendship requests are to be blocked.
     *                 Must not be null, as enforced by the {@link NotNull} validation.
     * @return a {@link ResponseEntity} containing a {@link FriendshipResponseDto}
     *         with the outcome of the block operation and the appropriate HTTP status.
     */

    @Operation(summary = "Block friendship requests from User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    @PutMapping("/block/{senderId}/")
    public ResponseEntity<FriendshipResponseDto> blockFriendshipRequests(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("senderId") @NotNull(message = "User ID must not be null.") Long senderId) {
        boolean isBlocked  = friendshipService.blockFriendshipRequestsFromUserById(senderId, userVO.getId());
        return (isBlocked)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship request blocked successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship request blockage was unsuccessful."));
    }

    /**
     * Retrieves a list of mutual friends between the current user and a specified target user.
     *
     * This method returns a list of mutual friends shared by the authenticated user
     * (represented by {@code userVO}) and another user specified by {@code targetUserId}.
     * The response is encapsulated in a {@link ResponseEntity} with status {@code 200 OK}
     * containing the list of mutual friends as {@link FriendCardDto} objects.
     *
     * @param userVO       the current authenticated user for whom mutual friends are being retrieved.
     *                     This parameter is injected and hidden from API documentation.
     * @param targetUserId the ID of the user with whom to find mutual friends.
     *                     Must not be null, conforming to the {@link NotNull} validation constraints.
     * @return a {@link ResponseEntity} containing a {@link List} of {@link FriendCardDto} objects representing mutual friends,
     *         along with an HTTP status of {@code 200 OK}.
     */
    @Operation(summary = "View a list of mutual friends")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/mutual/{targetUserId}/")
    public ResponseEntity<List<FriendCardDto>> getMutualFriends(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("targetUserId") @NotNull(message = "User ID must not be null.") Long targetUserId) {
        return  ResponseEntity.ok(friendshipService.getAllMutualFriendsByUserId(userVO.getId(), targetUserId));
    }

    /**
     * Retrieves a list of friendship requests for the current authenticated user.
     * This method fetches all friendship requests that have been received by the
     * authenticated user (represented by {@code userVO}). The response is provided
     * in a {@link ResponseEntity} with status {@code 200 OK}, containing a list of
     * {@link RequestedFriendshipDto} objects, each representing a friendship request.
     *
     * @param userVO the current authenticated user for whom friendship requests are being retrieved.
     *               This parameter is injected and hidden from API documentation.
     * @return a {@link ResponseEntity} containing a {@link List} of
     *         {@link RequestedFriendshipDto} objects representing the friendship requests,
     *         along with an HTTP status of {@code 200 OK}.
     */
    @Operation(summary = "View a list friendship requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
    })
    @GetMapping("/requests/")
    public ResponseEntity<List<RequestedFriendshipDto>> getFriendshipRequestsForUserById(
            @Parameter(hidden = true) @CurrentUser UserVO userVO) {
        return  ResponseEntity.ok(friendshipService.getAllFriendshipRequestsForUserById(userVO.getId()));
    }

    /**
     * Retrieves the friend page for a specified user.
     * This method allows the authenticated user (represented by {@code userVO})
     * to fetch the friend page for another user identified by {@code targetUserId}.
     * If the current user attempts to access their own friend page, a {@link WrongIdException}
     * is thrown. If the friend page is successfully assembled, the method returns a response
     * with status {@code 200 OK} containing a {@link FriendPageDto}. If the friendship
     * relationship is not found or an invalid ID is provided, it returns a {@code 404 Not Found} status.
     *
     * @param userVO       the current authenticated user trying to access the friend page.
     *                     This parameter is injected and hidden from API documentation.
     * @param targetUserId the ID of the user whose friend page is being retrieved.
     *                     Must not be null, as enforced by the {@link NotNull} validation.
     * @return a {@link ResponseEntity} containing a {@link FriendPageDto}
     *         representing the requested friend's page, or a {@code 404 Not Found} status if
     *         the friendship does not exist or the ID is invalid.
     * @throws WrongIdException if the current user attempts to view their own friend page.
     */
    @Operation(summary = "Get a FriendPageDto for a target user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/friendPage/{targetUserId}/")
    public ResponseEntity<FriendPageDto> getFriendPage(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @PathVariable("targetUserId") @NotNull(message = "User ID must not be null.") Long targetUserId) {
        Optional<FriendshipVO> friendship = friendshipService.findFriendshipByParticipantsId(userVO.getId(), targetUserId);
        if(userVO.getId().equals(targetUserId)) {
            throw new WrongIdException("Current User cannot view him/herself in stead of a Friend. Provide Friends id.");
        }
        try {
            return ResponseEntity.ok(friendPageService.assembleFriendPage(targetUserId, friendship.map(FriendshipVO::getId)));
        } catch (WrongIdException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves a paginated list of recommended friends for the current authenticated user.
     * This method fetches recommendations for potential friends that the authenticated user
     * (represented by {@code userVO}) may want to connect with. The results are returned in a
     * paginated format encapsulated within a {@link ResponseEntity} with status {@code 200 OK}.
     * The method takes a {@link Pageable} parameter to allow clients to specify pagination details
     * such as page size and number.
     *
     * @param userVO   the current authenticated user for whom friend recommendations are being requested.
     *                 This parameter is injected and hidden from API documentation.
     * @param pageable  the pagination information that defines the size and number of the requested page.
     * @return a {@link ResponseEntity} containing a {@link PageableDto}
     *         with a list of {@link FriendCardDto} objects representing recommended friends.
     */
    @Operation(summary = "Get recommended friends for a target user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/recommendedFriends/")
    public ResponseEntity<PageableDto<FriendCardDto>> getRecommendedFriends(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            Pageable pageable) {
        return  ResponseEntity.ok(friendshipService.recommendFriendsForUser(pageable, userVO.getId()));
    }

    /**
     * Retrieves a paginated list of friends for the current authenticated user, with optional filtering.
     * This method allows the authenticated user (represented by {@code userVO})
     * to fetch a list of friends. The results can be filtered based on the
     * criteria specified in the {@link FriendshipsFilterRequestDto}. The response
     * is returned in a paginated format within a {@link ResponseEntity}
     * with status {@code 200 OK}.
     *
     * @param userVO                    the current authenticated user whose friends are being retrieved.
     *                                  This parameter is injected and hidden from API documentation.
     * @param friendshipsFilterRequest   optional filtering criteria used to refine the list of friends.
     *                                  If no filtering is required, this may be omitted.
     * @return a {@link ResponseEntity} containing a {@link PageableDto}
     *         with a list of {@link FriendCardDto} objects representing the user's friends.
     */
    @Operation(summary = "Get a page of friends for a target user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
            @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
            @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping
    public ResponseEntity<PageableDto<FriendCardDto>> getFriendsOfUser(
            @Parameter(hidden = true) @CurrentUser UserVO userVO,
            @RequestBody(required = false) FriendshipsFilterRequestDto friendshipsFilterRequest) {
        return  ResponseEntity.ok(
                    friendshipService.getAllFriendsByUserId(
                        userVO.getId(),
                        friendshipsFilterRequest)
        );
    }
}
