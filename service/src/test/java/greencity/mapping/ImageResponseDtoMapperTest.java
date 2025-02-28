package greencity.mapping;

import greencity.dto.event.ImageResponseDto;
import greencity.entity.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ImageResponseDtoMapperTest {
    private final ImageResponseDtoMapper mapper = new ImageResponseDtoMapper();

    @Test
    void convertTest() {
        Image image = Image.builder()
            .id(1L)
            .imagePath("test/path/to/image.jpg")
            .build();

        ImageResponseDto result = mapper.convert(image);

        assertNotNull(result, "The result should not be null");
        assertEquals(image.getId(), result.getId(), "The ID should be mapped correctly");
        assertEquals(image.getImagePath(), result.getImagePath(), "The image path should be mapped correctly");
    }
}
