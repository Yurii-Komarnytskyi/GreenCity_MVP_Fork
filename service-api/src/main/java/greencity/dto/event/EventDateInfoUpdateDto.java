package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import greencity.annotations.ConsistentDateTime;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConsistentDateTime
public class EventDateInfoUpdateDto implements EventDateInfoDto {

    private Long id;

    @NotNull(message = "Event date is mandatory.")
    @FutureOrPresent(message = "Event date cannot be in the past.")
    private LocalDate eventDate;

    @Future(message = "Event start time must be in the future.")
    private LocalDateTime eventTimeStart;

    @Future(message = "Event end time must be in the future.")
    private LocalDateTime eventTimeEnd;

    @NotNull(message = "All day flag is mandatory.")
    @JsonProperty("isAllDay")
    private Boolean isAllDay;

    @NotNull(message = "Place flag is mandatory.")
    @JsonProperty("isPlace")
    private Boolean isPlace;

    @NotNull(message = "Online flag is mandatory.")
    @JsonProperty("isOnline")
    private Boolean isOnline;

    @Size(max = 255, message = "Location cannot exceed 255 characters.")
    private String location;

    @Size(max = 2083, message = "URL length cannot exceed 2083 characters.")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Invalid URL format.")
    private String url;

    private int numOfTheDay;
}
