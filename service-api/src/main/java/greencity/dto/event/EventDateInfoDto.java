package greencity.dto.event;

import java.time.LocalDateTime;

public interface EventDateInfoDto {
    Boolean getIsOnline();

    String getUrl();

    Boolean getIsPlace();

    String getLocation();

    LocalDateTime getEventTimeStart();

    LocalDateTime getEventTimeEnd();
}
