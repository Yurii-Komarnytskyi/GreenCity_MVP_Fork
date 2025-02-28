package greencity.mapping;

import greencity.dto.event.EventCommentResponseDto;
import greencity.dto.event.EventResponseDto;
import greencity.dto.event.ImageResponseDto;
import greencity.dto.event.InitiativeTypeResponseDto;
import greencity.dto.user.AuthorDto;
import greencity.entity.Event;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class EventResponseDtoMapper extends AbstractConverter<Event, EventResponseDto> {
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    protected EventResponseDto convert(Event event) {

        return EventResponseDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .description(event.getDescription())
            .duration(event.getDuration())
            .initiativeTypes(event.getInitiativeTypes().stream()
                .map(initiativeType -> InitiativeTypeResponseDto.builder()
                    .id(initiativeType.getId())
                    .name(initiativeType.getName())
                    .build())
                .toList())
            .isOpen(event.isOpen())
            .images(event.getImages().stream()
                .map(image -> ImageResponseDto.builder()
                    .id(image.getId())
                    .imagePath(image.getImagePath())
                    .build())
                .toList())
            .author(AuthorDto.builder()
                .id(event.getAuthor().getId())
                .name(event.getAuthor().getName())
                .build())
            .creationDate(event.getCreationDate())
            .mainImage(
                event.getMainImage() != null ? modelMapper.map(event.getMainImage(), ImageResponseDto.class) : null)
            .rating(event.getRating())
            .likes(event.getLikes())
            .build();
    }
}
