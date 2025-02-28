package greencity.dto.event;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class ImageRequestDto {
    @Pattern(
        regexp = ".*\\.(jpg|png)$",
        message = "Image should be in the .jpg or .png. format")
    private String imagePath;
}
