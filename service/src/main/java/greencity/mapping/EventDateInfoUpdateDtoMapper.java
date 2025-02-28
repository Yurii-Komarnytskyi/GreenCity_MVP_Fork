package greencity.mapping;

import greencity.dto.event.EventDateInfoUpdateDto;
import greencity.entity.EventDateInfo;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EventDateInfoUpdateDtoMapper extends AbstractConverter<EventDateInfoUpdateDto, EventDateInfo> {
    @Override
    protected EventDateInfo convert(EventDateInfoUpdateDto dto) {
        EventDateInfo.EventDateInfoBuilder builder = EventDateInfo.builder()
            .eventDate(dto.getEventDate())
            .eventTimeStart(dto.getEventTimeStart())
            .eventTimeEnd(dto.getEventTimeEnd())
            .isAllDay(dto.getIsAllDay())
            .isPlace(dto.getIsPlace())
            .isOnline(dto.getIsOnline())
            .location(dto.getLocation())
            .numOfDayInEvent(dto.getNumOfTheDay())
            .url(dto.getUrl());

        if (dto.getId() != null) {
            builder.id(dto.getId());
        }

        return builder.build();
    }
}
