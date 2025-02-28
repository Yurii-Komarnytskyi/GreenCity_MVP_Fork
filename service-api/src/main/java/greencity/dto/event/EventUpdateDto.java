package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class EventUpdateDto {
    @NotBlank(message = "Title cannot be empty")
    @Size(max = 70, message = "Event title must be up to 70 characters")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    @Size(
        min = 20,
        max = 63206,
        message = "Event description must contain more than 20 characters but less than 63,206 characters")
    private String description;

    private String authorEmail;

    @Max(7)
    private int duration = 1;

    @Size(
        min = 1,
        max = 7,
        message = "The number of event days must be between 1 and 7")
    @Valid
    private List<EventDateInfoUpdateDto> eventDays;

    @Size(
        min = 1,
        max = 3,
        message = "The number of initiative types must be between 1 and 3")
    private List<InitiativeTypeRequestDto> initiativeTypes;

    @JsonProperty("isOpen")
    private boolean isOpen = true;

    @Size(
        max = 5,
        message = "The number of images must be up to 5")
    @Valid
    private List<ImageRequestDto> images;
    private ImageRequestDto mainImage;
}
