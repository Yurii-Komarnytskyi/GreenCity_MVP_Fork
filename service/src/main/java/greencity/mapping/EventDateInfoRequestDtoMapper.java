package greencity.mapping;

import greencity.dto.event.EventDateInfoRequestDto;
import greencity.entity.EventDateInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EventDateInfoRequestDtoMapper extends AbstractConverter<EventDateInfoRequestDto, EventDateInfo> {

    @Override
    protected EventDateInfo convert(EventDateInfoRequestDto dto) {
        return EventDateInfo.builder()
            .eventDate(dto.getEventDate())
            .eventTimeStart(dto.getEventTimeStart())
            .eventTimeEnd(dto.getEventTimeEnd())
            .isAllDay(dto.getIsAllDay())
            .isPlace(dto.getIsPlace())
            .isOnline(dto.getIsOnline())
            .location(dto.getLocation())
            .url(dto.getUrl())
            .build();
    }

}
