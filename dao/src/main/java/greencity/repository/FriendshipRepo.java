package greencity.repository;

import greencity.entity.Friendship;
import greencity.entity.User;
import greencity.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

    /**
     * Retrieves a list of users who have friends in common with the user identified by the provided user ID.
     *
     * @param userId the ID of the user for whom to find common friends.
     * @return a list of {@link User} entities that have mutual friendships with the specified user.
     */
    @Query("SELECT DISTINCT f.user FROM Friendship f " +
            "WHERE f.friend.id IN (SELECT f2.friend.id FROM Friendship f2 WHERE f2.user.id = :userId) " +
            "AND f.user.id != :userId AND f.status = :status ")

    List<User> findUsersWithCommonFriendsInStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    /**
     * Retrieves a {@link Friendship} by its unique identifier.
     *
     * @param id the unique identifier of the {@link Friendship} to be retrieved.
     * @return an {@link Optional} containing the {@link Friendship} if found,
     *         or an empty {@link Optional} if no friendship exists with the provided id.
     */
    Optional<Friendship> findFriendshipById(Long id);

    /**
     * Retrieves a list of all {@link Friendship} entities associated with the specified user.
     *
     * @param id the unique identifier of the {@link User} whose friendships are to be retrieved.
     * @return a list of {@link Friendship} instances related to the specified user.
     *         If the user has no friendships, an empty list is returned.
     */
    List<Friendship> getAllFriendshipsByUserId(Long id);

    /**
     * Finds a Friendship entity by either user ID and friend ID or vice versa.
     *
     * @param userId   the ID of one user
     * @param friendId the ID of the other user
     * @return the Friendship entity if found, or null if not found
     */
    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId AND f.friend.id = :friendId) " +
        "OR (f.user.id = :friendId AND f.friend.id = :userId)")
    Optional<Friendship> findFriendshipByEitherUserId(@Param("userId") Long userId, @Param("friendId") Long friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :senderId AND f.friend.id = :recipientId) ")
    Optional<Friendship> findFriendshipByUserIdOrderSensitive(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);

    /**
     * Retrieves a list of friendship requests for a specified user. This method
     * returns all pending friendship requests that have been sent to the user
     * identified by the provided user ID. The list will contain instances of the
     * Friendship entity, representing the users who have requested to befriend the
     * specified user.
     *
     * @param recipientId the ID of the user for whom to retrieve friendship requests
     * @return a list of Friendship entities representing all pending friendship requests
     * for the specified user. If no requests are found, an empty list is returned.
     */
    @Query("SELECT f FROM Friendship f WHERE f.friend.id = :recipientId AND f.status = 'REQUESTED'")
    List<Friendship> getFriendshipRequestsByUserId(Long recipientId);
}
