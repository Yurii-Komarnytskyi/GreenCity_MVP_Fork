package greencity.mapping;

import greencity.dto.event.ImageResponseDto;
import greencity.entity.Image;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ImageResponseDtoMapper extends AbstractConverter<Image, ImageResponseDto> {
    @Override
    protected ImageResponseDto convert(Image image) {
        if (image == null) {
            return null;
        }

        return ImageResponseDto.builder()
            .id(image.getId())
            .imagePath(image.getImagePath())
            .build();
    }
}
