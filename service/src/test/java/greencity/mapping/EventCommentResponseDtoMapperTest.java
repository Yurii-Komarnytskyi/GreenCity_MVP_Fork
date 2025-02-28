package greencity.mapping;

import greencity.dto.event.EventCommentResponseDto;
import greencity.entity.User;
import greencity.entity.EventComment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventCommentResponseDtoMapperTest {
    private final EventCommentResponseDtoMapper mapper = new EventCommentResponseDtoMapper();

    @Test
    void convertTest() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        EventComment parentComment = new EventComment();
        parentComment.setId(10L);

        EventComment eventComment = new EventComment();
        eventComment.setId(5L);
        eventComment.setText("Test Comment");
        eventComment.setCreatedDate(LocalDateTime.of(2023, 1, 1, 12, 0));
        eventComment.setModifiedDate(LocalDateTime.of(2023, 1, 2, 12, 0));
        eventComment.setUser(user);
        eventComment.setParentComment(parentComment);

        EventCommentResponseDto result = mapper.convert(eventComment);

        assertNotNull(result, "The result should not be null");
        assertEquals(eventComment.getId(), result.getId(), "The id should be mapped correctly");
        assertEquals(eventComment.getText(), result.getText(), "The text should be mapped correctly");
        assertEquals(eventComment.getCreatedDate(), result.getCreatedDate(),
            "The created date should be mapped correctly");
        assertEquals(eventComment.getModifiedDate(), result.getModifiedDate(),
            "The modified date should be mapped correctly");
        assertEquals(eventComment.getUser().getId(), result.getAuthor().getId(),
            "The author should be mapped correctly");
        assertEquals(eventComment.getParentComment().getId(), result.getParentCommentId(),
            "The parent comment ID should be mapped correctly");
    }
}
