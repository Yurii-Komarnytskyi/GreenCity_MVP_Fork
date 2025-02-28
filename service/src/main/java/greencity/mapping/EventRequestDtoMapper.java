package greencity.mapping;

import greencity.dto.event.EventRequestDto;
import greencity.entity.Event;
import greencity.entity.InitiativeType;
import greencity.entity.Image;
import lombok.AllArgsConstructor;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EventRequestDtoMapper extends AbstractConverter<EventRequestDto, Event> {
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    protected Event convert(EventRequestDto eventRequestDto) {

        return Event.builder()
            .title(eventRequestDto.getTitle())
            .description(eventRequestDto.getDescription())
            .duration(eventRequestDto.getDuration())
            .initiativeTypes(eventRequestDto.getInitiativeTypes().stream()
                .map(dto -> InitiativeType.builder()
                    .name(dto.getName())
                    .build())
                .toList())
            .isOpen(eventRequestDto.isOpen())
            .images(eventRequestDto.getImages() != null ? eventRequestDto.getImages().stream()
                .map(dto -> modelMapper.map(dto, Image.class)).collect(Collectors.toSet()) : null)
            .mainImage(
                eventRequestDto.getMainImage() != null ? modelMapper.map(eventRequestDto.getMainImage(), Image.class)
                    : null)
            .build();
    }
}
