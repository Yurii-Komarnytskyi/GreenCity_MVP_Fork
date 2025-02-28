package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.friendship.FriendCardDto;
import greencity.dto.friendship.RequestedFriendshipDto;
import greencity.dto.user.UserVO;
import greencity.enums.FriendshipStatus;

import java.util.List;
import java.util.Optional;

public interface FriendshipService {

    /**
     * Retrieves a list of all friends for a specified user.
     *
     * @param id the ID of the user for whom to retrieve friends
     * @return a list of FriendCardDto objects representing the user's friends
     */
    List<FriendCardDto> getAllFriendsByUserId(Long id);

    /**
     * Retrieves a list of all friendship requests for a specified user. This method
     * provides a view of incoming friendship requests that have been sent to the
     * user, allowing them to view and manage pending relationships.
     *
     * @param userId the ID of the user for whom to retrieve friendship requests
     * @return a list of FriendshipRequestDto objects representing users who have
     *         sent friendship requests to the specified user
     */
    List<RequestedFriendshipDto> getAllFriendshipRequestsForUserById(Long userId);

    /**
     * Retrieves a list of mutual friends between two specified users.
     *
     * @param userId       the ID of the first user
     * @param targetUserId the ID of the second user whose mutual friends are sought
     * @return a list of FriendCardDto objects representing the mutual friends
     */
    List<FriendCardDto> getAllMutualFriendsByUserId(Long userId, Long targetUserId);

    /**
     * Proposes a page of potential friends for a specified user based on criteria.
     *
     * @param user the UserVO object for whom to propose friends
     * @return a list of FriendCardDto objects representing proposed friends
     */
    PageableDto<FriendCardDto> recommendFriendsForUser(UserVO user);

    /**
     * Sends a friendship request from one user to another.
     *
     * @param senderId    the ID of the user sending the request
     * @param recipientId the ID of the user receiving the request
     * @return true if request was successful, false otherwise
     */
    boolean requestFriendshipByUserId(Long senderId, Long recipientId);

    /**
     * Cancels a friendship request sent by a user to another user.
     *
     * @param senderId    the ID of the user who sent the friendship request
     * @param recipientId the ID of the user to whom the request was sent
     * @return true if friendship was canceled successfully, false otherwise
     */
    boolean cancelFriendshipRequestByUserId(Long senderId, Long recipientId);

    /**
     * Accepts a friendship request from one user to another.
     *
     * @param senderId    the ID of the user who sent the friendship request
     * @param recipientId the ID of the user accepting the friendship request
     * @return true if the request was accepted successfully, false otherwise
     */
    boolean acceptFriendshipRequestByUserId(Long senderId, Long recipientId);

    /**
     * Declines a friendship request from one user to another.
     *
     * @param senderId    the ID of the user who sent the friendship request
     * @param recipientId the ID of the user declining the friendship request
     * @return true if the request was declined successfully, false otherwise
     */
    boolean declineFriendshipRequestByUserId(Long senderId, Long recipientId);

    /**
     * Retrieves the friendship request status between two specified users.
     *
     * @param userId       the ID of one user involved in the friendship request
     * @param targetUserId the ID of the other user involved in the friendship
     *                     request
     * @return the FriendshipRequestStatus representing the current status of the
     *         request
     */
    Optional<FriendshipStatus> getFriendshipStatusByUserId(Long userId, Long targetUserId);

    /**
     * Deletes a friendship between two specified users.
     *
     * @param userId   the ID of the user who wants to delete the friendship
     * @param friendId the ID of the friend to be deleted
     * @return true if the friendship was deleted successfully, false otherwise
     */
    boolean deleteFriendByUserId(Long userId, Long friendId);

    /**
     * Check if two users are friends based on user IDs.
     *
     * @param userId   the ID of the first user
     * @param friendId the ID of the second user
     * @return true if they are friends, false otherwise
     */
    boolean areFriends(Long userId, Long friendId);

    /**
     * Blocks friendship requests from a specified user. This method prevents the
     * specified user (sender) from sending friendship requests to the recipient
     * user. It can be used to manage unwanted requests and relationships.
     *
     * @param senderId    the ID of the user whose friendship requests will be
     *                    blocked
     * @param recipientId the ID of the user who is blocking the requests
     * @return true if the operation was successful and the user is now blocked from
     *         sending friendship requests; false otherwise
     */
    boolean blockFriendshipRequestsFromUserById(Long senderId, Long recipientId);

}
