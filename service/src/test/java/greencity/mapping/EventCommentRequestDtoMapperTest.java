package greencity.mapping;

import greencity.dto.event.EventCommentRequestDto;
import greencity.entity.EventComment;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EventCommentRequestDtoMapperTest {

    @Test
    void convertTest() {
        EventCommentRequestDtoMapper mapper = new EventCommentRequestDtoMapper();

        EventCommentRequestDto toConvert = EventCommentRequestDto.builder()
            .text("Test text")
            .build();

        EventComment actualComment = mapper.convert(toConvert);

        assertNotNull(actualComment, "The result should not be null");
        assertEquals(toConvert.getText(), actualComment.getText(), "The result should be the same");
        assertNotNull(actualComment.getCreatedDate(), "The result should not be null");
        assertTrue(actualComment.getCreatedDate().isBefore(LocalDateTime.now().plusSeconds(1)),
            "The created date should be set to the current time");
    }
}
