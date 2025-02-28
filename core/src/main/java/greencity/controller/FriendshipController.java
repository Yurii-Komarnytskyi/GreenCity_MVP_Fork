package greencity.controller;

import greencity.constant.HttpStatuses;
import greencity.dto.friendship.FriendCardDto;
import greencity.dto.friendship.FriendshipResponseDto;
import greencity.dto.friendship.RequestedFriendshipDto;
import greencity.service.FriendshipService;
import greencity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/friends")
public class FriendshipController {

    FriendshipService friendshipService;
    UserService userService;

    @Autowired
    public FriendshipController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    /**
     * Requests a friendship from the sender to the recipient.
     *
     * If the senderId does not match the currently authenticated user, a FORBIDDEN
     * response is returned.
     *
     * @param senderId    the ID of the user sending the friendship request
     * @param recipientId the ID of the user receiving the friendship request
     * @return a ResponseEntity containing the response message and status code
     */

    @Operation(summary = "Request friendship")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = HttpStatuses.CREATED),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    @PostMapping("/{senderId}/request/{recipientId}/")
    public ResponseEntity<FriendshipResponseDto> requestFriendship(
        @PathVariable("senderId") @NotNull(message = "User ID must not be null.") Long senderId,
        @PathVariable("recipientId") @NotNull(message = "User ID must not be null.") Long recipientId) {
        if (isNotCurrentUser(senderId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isRequested = friendshipService.requestFriendshipByUserId(senderId, recipientId);

        return (isRequested)
            ? ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new FriendshipResponseDto(true, "Friendship request sent successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship request already exists or cannot be sent."));
    }

    /**
     * Accepts a friendship request from a specified sender.
     *
     * If the recipientId does not match the currently authenticated user, a
     * FORBIDDEN response is returned.
     *
     * @param senderId    the ID of the user who sent the friendship request
     * @param recipientId the ID of the user accepting the friendship request
     * @return a ResponseEntity containing the response message and status code
     */

    @Operation(summary = "Accept friendship request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    // TODO REVERSE ARGS ???
    @PutMapping("/{senderId}/accept/{recipientId}/")
    public ResponseEntity<FriendshipResponseDto> acceptFriendship(
        @PathVariable("senderId") @NotNull(message = "User ID must not be null.") Long senderId,
        @PathVariable("recipientId") @NotNull(message = "User ID must not be null.") Long recipientId) {
        if (isNotCurrentUser(recipientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isAccepted = friendshipService.acceptFriendshipRequestByUserId(senderId, recipientId);
        return (isAccepted)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship accepted successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship record must exist and be in REQUESTED state"));
    }

    /**
     * Cancels a friendship request sent by the sender to the recipient.
     *
     * This method allows the user identified by the senderId to cancel a previously
     * sent friendship request to the user identified by the recipientId. If the
     * senderId does not match the currently authenticated user, a FORBIDDEN
     * response is returned.
     *
     * @param senderId    the ID of the user who sent the friendship request
     * @param recipientId the ID of the user to whom the friendship request was sent
     * @return a ResponseEntity containing the response message and status code
     */

    @Operation(summary = "Cancel friendship request")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    @PutMapping("/{senderId}/cancel/{recipientId}/")
    public ResponseEntity<FriendshipResponseDto> cancelFriendshipRequest(
        @PathVariable("senderId") @NotNull(message = "User ID must not be null.") Long senderId,
        @PathVariable("recipientId") @NotNull(message = "User ID must not be null.") Long recipientId) {
        if (isNotCurrentUser(senderId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isCancelled = friendshipService.cancelFriendshipRequestByUserId(senderId, recipientId);
        return (isCancelled)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship request cancelled successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship request cancellation has failed."));
    }

    /**
     * Deletes a friendship between the user and the specified friend.
     *
     * If either userId or friendId does not match the currently authenticated user,
     * a FORBIDDEN response is returned.
     *
     * @param userId   the ID of the user who wants to delete the friendship
     * @param friendId the ID of the user to be removed from friendships
     * @return a ResponseEntity containing the response message and status code
     */

    @Operation(summary = "Delete friendship record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @DeleteMapping("/{userId}/delete/{friendId}/")
    public ResponseEntity<FriendshipResponseDto> deleteFriendship(
        @PathVariable("userId") @NotNull(message = "User ID must not be null.") Long userId,
        @PathVariable("friendId") @NotNull(message = "User ID must not be null.") Long friendId) {
        if (isNotCurrentUser(userId) || isNotCurrentUser(friendId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isDeleted = friendshipService.deleteFriendByUserId(userId, friendId);
        return (isDeleted)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship deleted successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new FriendshipResponseDto(false, "Friendship record must exist and be in REQUESTED state"));
    }

    /**
     * Checks if two users are friends.
     *
     * If the userId does not match the currently authenticated user, a FORBIDDEN
     * response is returned.
     *
     * @param userId   the ID of the first user
     * @param friendId the ID of the second user
     * @return a ResponseEntity containing a FriendshipResponseDto indicating
     *         whether the friendship exists or not and an appropriate message.
     */

    @Operation(summary = "Check if two Users are friends")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "404", description = HttpStatuses.NOT_FOUND)
    })
    @GetMapping("/{userId}/areFriends/{friendId}/")
    public ResponseEntity<FriendshipResponseDto> checkFriendship(
        @PathVariable("userId") @NotNull(message = "User ID must not be null.") Long userId,
        @PathVariable("friendId") @NotNull(message = "User ID must not be null.") Long friendId) {
        if (isNotCurrentUser(userId) && isNotCurrentUser(friendId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean areFriends = friendshipService.areFriends(userId, friendId);
        return (areFriends)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship record exists and is in ACCEPTED state"))
            : ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new FriendshipResponseDto(false, "Friendship record must exist and be in ACCEPTED state"));
    }

    /**
     * Blocks friendship requests from a specified user.
     *
     * If the recipientId does not match the currently authenticated user, a
     * FORBIDDEN response is returned.
     *
     * @param senderId    the ID of the user sending the block request
     * @param recipientId the ID of the user whose requests are being blocked
     * @return a ResponseEntity containing a FriendshipResponseDto indicating
     *         whether the blocking was successful and an appropriate message.
     */

    @Operation(summary = "Block friendship requests from User")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
        @ApiResponse(responseCode = "406", description = HttpStatuses.NOT_ACCEPTABLE)
    })
    @PutMapping("/{senderId}/block/{recipientId}/")
    public ResponseEntity<FriendshipResponseDto> blockFriendshipRequests(
        @PathVariable("senderId") @NotNull(message = "User ID must not be null.") Long senderId,
        @PathVariable("recipientId") @NotNull(message = "User ID must not be null.") Long recipientId) {
        if (isNotCurrentUser(recipientId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        boolean isBlocked = friendshipService.blockFriendshipRequestsFromUserById(senderId, recipientId);
        return (isBlocked)
            ? ResponseEntity.ok(new FriendshipResponseDto(true, "Friendship request blocked successfully."))
            : ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(new FriendshipResponseDto(false, "Friendship request blockage was unsuccessful."));
    }

    /**
     * Retrieves a list of mutual friends between the specified users.
     *
     * If either userId or targetUserId does not match the currently authenticated
     * user, a FORBIDDEN response is returned.
     *
     * @param userId       the ID of the first user
     * @param targetUserId the ID of the second user
     * @return a ResponseEntity containing a list of FriendCardDto representing
     *         mutual friends, if found.
     */

    @Operation(summary = "View a list of mutual friends")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/{userId}/mutual/{targetUserId}/")
    public ResponseEntity<List<FriendCardDto>> getMutualFriends(
        @PathVariable("userId") @NotNull(message = "User ID must not be null.") Long userId,
        @PathVariable("targetUserId") @NotNull(message = "User ID must not be null.") Long targetUserId) {
        if (isNotCurrentUser(userId) && isNotCurrentUser(targetUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(friendshipService.getAllMutualFriendsByUserId(userId, targetUserId));
    }

    /**
     * Retrieves a list of friendship requests for a specified user.
     *
     * The list will contain instances of the Friendship entity, representing the
     * users who have requested to befriend the specified user.
     *
     * @param userId the ID of the user for whom to retrieve friendship requests
     * @return a ResponseEntity containing a list of RequestedFriendshipDto objects
     *         representing all pending friendship requests for the specified user.
     */

    @Operation(summary = "View a list friendship requests")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = HttpStatuses.OK),
        @ApiResponse(responseCode = "401", description = HttpStatuses.UNAUTHORIZED),
        @ApiResponse(responseCode = "403", description = HttpStatuses.FORBIDDEN),
    })
    @GetMapping("/{userId}/requested/")
    public ResponseEntity<List<RequestedFriendshipDto>> getFriendshipRequestsForUserById(
        @PathVariable("userId") @NotNull(message = "User ID must not be null.") Long userId) {
        if (isNotCurrentUser(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(friendshipService.getAllFriendshipRequestsForUserById(userId));
    }

    protected boolean isNotCurrentUser(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return !(Objects.nonNull(authentication)
            && authentication.isAuthenticated()
            && Objects.nonNull(userService.findIdByEmail(authentication.getPrincipal().toString()))
            && userService.findIdByEmail(authentication.getPrincipal().toString()).equals(id));
    }
}
