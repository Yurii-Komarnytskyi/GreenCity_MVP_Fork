package greencity.service;

import greencity.dto.event.EventCommentLikesRequestDto;
import greencity.dto.event.EventCommentLikesResponseDto;
import greencity.repository.EventCommentLikesRepo;
import greencity.repository.EventCommentRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventCommentLikesServiceImpl implements EventCommentLikesService {
    private EventCommentLikesRepo eventCommentLikesRepo;
    private EventCommentRepo eventCommentRepo;

    @Override
    public EventCommentLikesResponseDto likeOrDislikeComment(Long eventCommentId, Long userId,
        EventCommentLikesRequestDto eventCommentLikesRequestDto) {
        // this method is yet to be implemented
        return null;
    }

    @Override
    public List<Long> getUsersByEventCommentId(Long eventCommentId) {
        // this method is yet to be implemented
        return List.of();
    }

    @Override
    public List<Long> getEventCommentsByUserId(Long userId) {
        // this method is yet to be implemented
        return List.of();
    }

    @Override
    public long countLikesByEventCommentId(Long eventCommentId) {
        eventCommentRepo.findById(eventCommentId)
            .orElseThrow(() -> new EntityNotFoundException("EventComment not found with id: " + eventCommentId));

        return eventCommentLikesRepo.countLikesByEventCommentId(eventCommentId);
    }
}
