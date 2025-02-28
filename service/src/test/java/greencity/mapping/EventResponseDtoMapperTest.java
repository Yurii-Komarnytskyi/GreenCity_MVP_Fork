package greencity.mapping;

import greencity.dto.event.EventResponseDto;
import greencity.entity.*;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventResponseDtoMapperTest {
    private final EventResponseDtoMapper mapper = new EventResponseDtoMapper();

    @Test
    void convertTest() {
        EventComment comment = new EventComment();
        comment.setId(4L);

        Image image = new Image();
        image.setId(2L);
        image.setImagePath("imagePath");

        InitiativeType initiativeType = new InitiativeType();
        initiativeType.setId(3L);
        initiativeType.setName("Test Initiative Type");

        User author = new User();
        author.setId(1L);
        author.setName("author");

        Event event = Event.builder()
            .id(1L)
            .title("Test Event")
            .description("Test Event Description")
            .duration(5)
            .initiativeTypes(List.of(initiativeType))
            .isOpen(true)
            .images(Set.of(image))
            .author(author)
            .creationDate(ZonedDateTime.now())
            .comments(List.of(comment))
            .likes(10)
            .build();

        EventResponseDto eventResponseDto = mapper.convert(event);

        assertNotNull(eventResponseDto);
        assertEquals(event.getId(), eventResponseDto.getId());
        assertEquals(event.getTitle(), eventResponseDto.getTitle());
        assertEquals(event.getDescription(), eventResponseDto.getDescription());
        assertEquals(event.getDuration(), eventResponseDto.getDuration());
        assertEquals(event.isOpen(), eventResponseDto.isOpen());
        assertEquals(1, eventResponseDto.getInitiativeTypes().size());
        assertEquals(1, eventResponseDto.getImages().size());
        assertEquals(event.getAuthor().getId(), eventResponseDto.getAuthor().getId());
        assertEquals(event.getAuthor().getName(), eventResponseDto.getAuthor().getName());
        assertEquals(event.getLikes(), eventResponseDto.getLikes());
    }
}
