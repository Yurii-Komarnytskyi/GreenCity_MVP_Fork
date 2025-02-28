package greencity.mapping;

import greencity.dto.event.EventDateInfoResponseDto;
import greencity.entity.EventDateInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EventDateInfoResponseDtoMapper extends AbstractConverter<EventDateInfo, EventDateInfoResponseDto> {
    @Override
    protected EventDateInfoResponseDto convert(EventDateInfo eventDateInfo) {
        return EventDateInfoResponseDto.builder()
            .id(eventDateInfo.getId())
            .eventDate(eventDateInfo.getEventDate())
            .eventTimeStart(eventDateInfo.getEventTimeStart())
            .eventTimeEnd(eventDateInfo.getEventTimeEnd())
            .isAllDay(eventDateInfo.isAllDay())
            .isPlace(eventDateInfo.isPlace())
            .isOnline(eventDateInfo.isOnline())
            .location(eventDateInfo.getLocation())
            .url(eventDateInfo.getUrl())
            .numOfDayInEvent(eventDateInfo.getNumOfDayInEvent())
            .build();
    }
}
