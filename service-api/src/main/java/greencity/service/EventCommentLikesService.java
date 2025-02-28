package greencity.service;

import greencity.dto.event.EventCommentLikesRequestDto;
import greencity.dto.event.EventCommentLikesResponseDto;
import java.util.List;

public interface EventCommentLikesService {

    // Here I made some comments as i like))
    // I mean, that's more for understanding and i like it) looks professional)

    /**
     * Method to like or dislike an event comment.
     *
     * @param eventCommentId              the ID of the event comment.
     * @param userId                      the ID of the user liking/disliking the
     *                                    comment.
     * @param eventCommentLikesRequestDto the request data for liking/disliking.
     * @return the updated {@link EventCommentLikesResponseDto}.
     */
    EventCommentLikesResponseDto likeOrDislikeComment(Long eventCommentId, Long userId,
        EventCommentLikesRequestDto eventCommentLikesRequestDto);

    /**
     * Method to get all users who liked or disliked a specific event comment.
     *
     * @param eventCommentId the ID of the event comment.
     * @return a list of user IDs who liked/disliked the comment.
     */
    List<Long> getUsersByEventCommentId(Long eventCommentId);

    /**
     * Method to get all event comments liked or disliked by a specific user.
     *
     * @param userId the ID of the user.
     * @return a list of event comment IDs.
     */
    List<Long> getEventCommentsByUserId(Long userId);

    long countLikesByEventCommentId(Long eventCommentId);
}
