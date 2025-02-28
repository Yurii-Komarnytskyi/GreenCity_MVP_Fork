package greencity.mapping;

import greencity.dto.event.EventDateInfoResponseDto;
import greencity.entity.EventDateInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventDateInfoResponseDtoMapperTest {
    private final EventDateInfoResponseDtoMapper mapper = new EventDateInfoResponseDtoMapper();

    @Test
    void convertTest() {
        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .id(1L)
            .eventDate(LocalDate.of(2025, 1, 1))
            .eventTimeStart(LocalDateTime.of(2025, 1, 1, 10, 0))
            .eventTimeEnd(LocalDateTime.of(2025, 1, 1, 12, 0))
            .isAllDay(false)
            .isPlace(true)
            .isOnline(false)
            .location("Test Location")
            .numOfDayInEvent(2)
            .build();

        EventDateInfoResponseDto dto = mapper.convert(eventDateInfo);

        assertEquals(eventDateInfo.getId(), dto.getId());
        assertEquals(eventDateInfo.getEventDate(), dto.getEventDate());
        assertEquals(eventDateInfo.getEventTimeStart(), dto.getEventTimeStart());
        assertEquals(eventDateInfo.getEventTimeEnd(), dto.getEventTimeEnd());
        assertEquals(eventDateInfo.isAllDay(), dto.isAllDay());
        assertEquals(eventDateInfo.isPlace(), dto.isPlace());
        assertEquals(eventDateInfo.isOnline(), dto.isOnline());
        assertEquals(eventDateInfo.getLocation(), dto.getLocation());
        assertEquals(eventDateInfo.getUrl(), dto.getUrl());
        assertEquals(eventDateInfo.getNumOfDayInEvent(), dto.getNumOfDayInEvent());
    }
}
