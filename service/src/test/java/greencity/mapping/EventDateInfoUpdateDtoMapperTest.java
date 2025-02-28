package greencity.mapping;

import greencity.dto.event.EventDateInfoUpdateDto;
import greencity.entity.EventDateInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EventDateInfoUpdateDtoMapperTest {

    private final EventDateInfoUpdateDtoMapper mapper = new EventDateInfoUpdateDtoMapper();

    @Test
    void convertTest() {
        EventDateInfoUpdateDto dto = EventDateInfoUpdateDto.builder()
            .id(1L)
            .eventDate(LocalDate.of(2025, 2, 16))
            .eventTimeStart(LocalDateTime.of(2025, 2, 16, 10, 0))
            .eventTimeEnd(LocalDateTime.of(2025, 2, 16, 12, 0))
            .isAllDay(false)
            .isPlace(true)
            .isOnline(false)
            .location("Sample Location")
            .numOfTheDay(1)
            .url("http://example.com")
            .build();

        EventDateInfo entity = mapper.convert(dto);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(LocalDate.of(2025, 2, 16), entity.getEventDate());
        assertEquals(LocalDateTime.of(2025, 2, 16, 10, 0), entity.getEventTimeStart());
        assertEquals(LocalDateTime.of(2025, 2, 16, 12, 0), entity.getEventTimeEnd());
        assertFalse(entity.isAllDay());
        assertTrue(entity.isPlace());
        assertFalse(entity.isOnline());
        assertEquals("Sample Location", entity.getLocation());
        assertEquals(1, entity.getNumOfDayInEvent());
        assertEquals("http://example.com", entity.getUrl());
    }

    @Test
    void convertTest_NullId() {
        EventDateInfoUpdateDto dto = EventDateInfoUpdateDto.builder()
            .eventDate(LocalDate.of(2025, 2, 16))
            .eventTimeStart(LocalDateTime.of(2025, 2, 16, 10, 0))
            .eventTimeEnd(LocalDateTime.of(2025, 2, 16, 12, 0))
            .isAllDay(true)
            .isPlace(false)
            .isOnline(true)
            .location("Another Location")
            .numOfTheDay(2)
            .url("http://test.com")
            .build();

        EventDateInfo entity = mapper.convert(dto);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals(LocalDate.of(2025, 2, 16), entity.getEventDate());
        assertEquals(LocalDateTime.of(2025, 2, 16, 10, 0), entity.getEventTimeStart());
        assertEquals(LocalDateTime.of(2025, 2, 16, 12, 0), entity.getEventTimeEnd());
        assertTrue(entity.isAllDay());
        assertFalse(entity.isPlace());
        assertTrue(entity.isOnline());
        assertEquals("Another Location", entity.getLocation());
        assertEquals(2, entity.getNumOfDayInEvent());
        assertEquals("http://test.com", entity.getUrl());
    }

}
