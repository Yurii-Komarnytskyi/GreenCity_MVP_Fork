package greencity.mapping;

import greencity.dto.event.EventProfilePreviewDto;
import greencity.dto.event.ImageResponseDto;
import greencity.dto.event.InitiativeTypeResponseDto;
import greencity.dto.user.AuthorDto;
import greencity.dto.user.UserProfilePictureDto;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class EventProfilePreviewDtoMapper extends AbstractConverter<EventMappingContext, EventProfilePreviewDto> {
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    protected EventProfilePreviewDto convert(EventMappingContext eventMappingContext) {
        return EventProfilePreviewDto.builder()
            .id(eventMappingContext.getEvent().getId())
            .title(eventMappingContext.getEvent().getTitle())
            .creationDate(eventMappingContext.getEvent().getCreationDate())
            .eventDate(eventMappingContext.getEventDateInfo().getEventDate())
            .eventTimeStart(eventMappingContext.getEventDateInfo().getEventTimeStart())
            .author(modelMapper.map(eventMappingContext.getEvent().getAuthor(), AuthorDto.class))
            .location(eventMappingContext.getEventDateInfo().isPlace()
                ? eventMappingContext.getEventDateInfo().getLocation()
                : "Online")
            .initiativeTypes(eventMappingContext.getEvent().getInitiativeTypes().stream().map(
                i -> modelMapper.map(i, InitiativeTypeResponseDto.class)).toList())
            .isOpen(eventMappingContext.getEvent().isOpen())
            .mainImage(modelMapper.map(eventMappingContext.getEvent().getMainImage(), ImageResponseDto.class))
            .rating(eventMappingContext.getEvent().getRating())
            .participants(eventMappingContext.getParticipants().stream()
                .map(p -> modelMapper.map(p, UserProfilePictureDto.class)).toList())
            .build();
    }
}
