package greencity.service;

import greencity.dto.event.EventLikesRequestDto;
import greencity.dto.event.EventLikesResponseDto;
import greencity.repository.EventLikesRepo;
import greencity.repository.EventRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EventLikeServiceImpl implements EventLikeService {
    private EventRepo eventRepo;
    private EventLikesRepo eventLikesRepo;

    @Override
    public void addLike(EventLikesRequestDto eventLikesRequestDto) {
        // This method is yet to be implemented
    }

    @Override
    public void removeLike(Long userId, Long eventId) {
        // This method is yet to be implemented
    }

    @Override
    public EventLikesResponseDto getLikesByEventId(Long eventId) {
        return null;
    }

    @Override
    public EventLikesResponseDto getLikesByUserId(Long userId) {
        // This method is yet to be implemented
        return null;
    }

    @Override
    public List<EventLikesResponseDto> getAllLikes() {
        // This method is yet to be implemented
        return List.of();
    }

    @Override
    public long countLikes(long eventId) {
        eventRepo.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        return eventLikesRepo.countLikesByEventId(eventId);
    }
}
