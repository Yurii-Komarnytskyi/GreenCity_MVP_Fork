package greencity.mapping;

import greencity.dto.event.EventDateInfoRequestDto;
import greencity.entity.EventDateInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EventDateInfoRequestDtoMapperTest {
    private final EventDateInfoRequestDtoMapper mapper = new EventDateInfoRequestDtoMapper();

    @Test
    void convertTest() {
        EventDateInfoRequestDto dto = EventDateInfoRequestDto.builder()
            .eventDate(LocalDate.of(2025, 1, 1))
            .eventTimeStart(LocalDateTime.of(2025, 1, 1, 10, 0))
            .eventTimeEnd(LocalDateTime.of(2025, 1, 1, 12, 0))
            .isAllDay(false)
            .isPlace(true)
            .isOnline(false)
            .location("Test location")
            .build();

        EventDateInfo result = mapper.convert(dto);

        assertNotNull(result, "The result should not be null");
        assertEquals(dto.getEventDate(), result.getEventDate(), "The event date should be mapped correctly");
        assertEquals(dto.getEventTimeStart(), result.getEventTimeStart(),
            "The event start time should be mapped correctly");
        assertEquals(dto.getEventTimeEnd(), result.getEventTimeEnd(), "The event end time should be mapped correctly");
        assertEquals(dto.getIsAllDay(), result.isAllDay(), "The isAllDay flag should be mapped correctly");
        assertEquals(dto.getIsPlace(), result.isPlace(), "The isPlace flag should be mapped correctly");
        assertEquals(dto.getIsOnline(), result.isOnline(), "The isOnline flag should be mapped correctly");
        assertEquals(dto.getLocation(), result.getLocation(), "The location should be mapped correctly");
        assertEquals(dto.getUrl(), result.getUrl(), "The URL should be mapped correctly");
    }
}
