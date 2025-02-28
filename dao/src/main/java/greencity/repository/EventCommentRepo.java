package greencity.repository;

import greencity.entity.Event;
import greencity.entity.EventComment;
import greencity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventCommentRepo extends JpaRepository<EventComment, Long> {

    /**
     * Method to get a EventComment by its ID.
     *
     * @param id ID of the EventComment.
     * @return an {@link Optional} of {@link EventComment}.
     */
    Optional<EventComment> findById(Long id);

    /**
     * Method to find all comments for a specific event.
     *
     * @param event the {@link Event} instance.
     * @return list of {@link EventComment} instances.
     */
    Page<EventComment> findByEvent(Event event, Pageable pageable);

    /**
     * Method to find all top-level comments (parent comments) for a specific event.
     *
     * @param event the {@link Event} instance.
     * @return list of top-level {@link EventComment} instances.
     */
    @Query("SELECT ec FROM EventComment ec WHERE ec.event = :event AND ec.parentComment IS NULL")
    List<EventComment> findTopLevelCommentsByEvent(Event event);

    /**
     * Method to find all comments written by a specific user.
     *
     * @param user the {@link User} instance.
     * @return list of {@link EventComment} instances.
     */
    List<EventComment> findByUser(User user);

    /**
     * Method to find all replies for a specific comment.
     *
     * @param parentComment the parent {@link EventComment} instance.
     * @return list of reply {@link EventComment} instances.
     */
    List<EventComment> findByParentComment(EventComment parentComment);

    /**
     * Method to find all non-deleted comments for a specific event.
     *
     * @param event the {@link Event} instance.
     * @return list of {@link EventComment} instances.
     */
    @Query("SELECT ec FROM EventComment ec WHERE ec.event = :event AND ec.deleted = false")
    List<EventComment> findNonDeletedCommentsByEvent(Event event);

    /**
     * Method to count the total number of comments (including replies) for a
     * specific event.
     *
     * @param event the {@link Event} instance.
     * @return total number of comments.
     */
    @Query("SELECT COUNT(ec) FROM EventComment ec WHERE ec.event = :event")
    long countByEvent(Event event);

    /**
     * Method to count the total number of replies for a specific parent comment.
     *
     * @param parentComment the parent {@link EventComment} instance.
     * @return total number of replies.
     */
    @Query("SELECT COUNT(ec) FROM EventComment ec WHERE ec.parentComment = :parentComment")
    long countRepliesByParentComment(EventComment parentComment);

    /**
     * Method to find all comments for a specific event, ordered by creation date in
     * descending order. This ensures that the newest comments appear first in the
     * list.
     *
     * @param event    the {@link Event} instance for which comments are being
     *                 retrieved.
     * @param pageable the {@link Pageable} object for pagination and sorting
     *                 options.
     * @return a {@link Page} of {@link EventComment} instances sorted by
     *         `createdDate` in descending order.
     */
    Page<EventComment> findByEventOrderByCreatedDateDesc(Event event, Pageable pageable);

}
