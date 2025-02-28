package greencity.mapping;

import greencity.dto.event.ImageRequestDto;
import greencity.entity.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageRequestDtoMapperTest {
    private final ImageRequestDtoMapper mapper = new ImageRequestDtoMapper();

    @Test
    void convertTest() {
        ImageRequestDto imageRequestDto = new ImageRequestDto();
        imageRequestDto.setImagePath("test/path/to/image.jpg");

        Image result = mapper.convert(imageRequestDto);

        assertNotNull(result, "The result should not be null");
        assertEquals(imageRequestDto.getImagePath(), result.getImagePath(),
            "The image path should be mapped correctly");
    }
}
