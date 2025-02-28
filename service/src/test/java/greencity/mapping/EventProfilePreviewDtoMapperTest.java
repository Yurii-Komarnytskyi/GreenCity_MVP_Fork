package greencity.mapping;

import greencity.dto.event.EventProfilePreviewDto;
import greencity.dto.user.AuthorDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventProfilePreviewDtoMapperTest {
    private final EventProfilePreviewDtoMapper mapper = new EventProfilePreviewDtoMapper();

    @Test
    void convertTest() {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Title");
        event.setDescription("Description");
        event.setMainImage(Image.builder().imagePath("path").build());
        event.setAuthor(User.builder().name("Ann").id(2L).build());
        event.setCreationDate(ZonedDateTime.of(2025, 12, 25, 10, 0, 0, 0, ZoneOffset.UTC));

        EventDateInfo eventDateInfo = new EventDateInfo();
        eventDateInfo.setId(1L);
        eventDateInfo.setEventTimeStart(LocalDateTime.of(2025, 12, 25, 10, 0));
        eventDateInfo.setEventDate(LocalDate.of(2025, 12, 25));

        User participant = new User();
        participant.setId(1L);
        participant.setFirstName("John");

        List<User> participants = List.of(participant);

        EventMappingContext eventMappingContext = new EventMappingContext(event, eventDateInfo, participants);

        EventProfilePreviewDto expected = EventProfilePreviewDto.builder()
            .id(1L)
            .title("Title")
            .creationDate(ZonedDateTime.of(2025, 12, 25, 10, 0, 0, 0, ZoneOffset.UTC))
            .eventDate(LocalDate.of(2025, 12, 25))
            .eventTimeStart(LocalDateTime.of(2025, 12, 25, 10, 0))
            .author(AuthorDto.builder().name("Ann").id(2L).build())
            .participants(List.of(UserProfilePictureDto.builder().id(1L).build()))
            .build();

        EventProfilePreviewDto result = mapper.convert(eventMappingContext);

        assertEquals(expected.getId(), result.getId());
        assertEquals(expected.getTitle(), result.getTitle());
        assertEquals(expected.getCreationDate(), result.getCreationDate());
        assertEquals(expected.getEventDate(), result.getEventDate());
        assertEquals(expected.getEventTimeStart(), result.getEventTimeStart());
    }
}
