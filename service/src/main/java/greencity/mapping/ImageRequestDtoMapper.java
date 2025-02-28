package greencity.mapping;

import greencity.dto.event.ImageRequestDto;
import greencity.entity.Image;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ImageRequestDtoMapper extends AbstractConverter<ImageRequestDto, Image> {
    @Override
    protected Image convert(ImageRequestDto imageRequestDto) {
        if (imageRequestDto == null) {
            return null;
        }

        return Image.builder()
            .imagePath(imageRequestDto.getImagePath())
            .build();
    }
}
