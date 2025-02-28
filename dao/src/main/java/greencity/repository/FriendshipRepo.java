package greencity.repository;

import greencity.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepo extends JpaRepository<Friendship, Long> {

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

    /**
     * Retrieves a list of friendship requests for a specified user. This method
     * returns all pending friendship requests that have been sent to the user
     * identified by the provided user ID. The list will contain instances of the
     * Friendship entity, representing the users who have requested to befriend the
     * specified user.
     *
     * @param userId the ID of the user for whom to retrieve friendship requests
     * @return a list of Friendship entities representing all pending friendship
     *         requests for the specified user. If no requests are found, an empty
     *         list is returned.
     */
    @Query("SELECT f FROM Friendship f WHERE f.friend.id = :recipientId AND f.status = 'REQUESTED'")
    List<Friendship> getFriendshipRequestsByUserId(Long recipientId);
}
