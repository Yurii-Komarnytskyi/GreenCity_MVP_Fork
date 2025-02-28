package greencity.mapping;

import greencity.dto.event.EventRequestDto;
import greencity.dto.event.ImageRequestDto;
import greencity.dto.event.InitiativeTypeRequestDto;
import greencity.entity.Event;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventRequestDtoMapperTest {
    private final EventRequestDtoMapper mapper = new EventRequestDtoMapper();

    @Test
    void convertTest() {
        EventRequestDto eventRequestDto = EventRequestDto.builder()
            .title("Test Event")
            .description("This is a test event.")
            .duration(3)
            .initiativeTypes(List.of(new InitiativeTypeRequestDto("Type 1"), new InitiativeTypeRequestDto("Type 2")))
            .isOpen(true)
            .images(List.of(new ImageRequestDto("image1.png"), new ImageRequestDto("image2.png")))
            .build();

        Event event = mapper.convert(eventRequestDto);

        assertNotNull(event);
        assertEquals("Test Event", event.getTitle());
        assertEquals("This is a test event.", event.getDescription());
        assertEquals(3, event.getDuration());
        assertEquals(2, event.getInitiativeTypes().size());
        assertTrue(event.getInitiativeTypes().stream().anyMatch(it -> it.getName().equals("Type 1")));
        assertTrue(event.getInitiativeTypes().stream().anyMatch(it -> it.getName().equals("Type 2")));
        assertTrue(event.isOpen());
        assertEquals(2, event.getImages().size());
        assertTrue(event.getImages().stream().anyMatch(image -> image.getImagePath().equals("image1.png")));
        assertTrue(event.getImages().stream().anyMatch(image -> image.getImagePath().equals("image2.png")));
    }
}
