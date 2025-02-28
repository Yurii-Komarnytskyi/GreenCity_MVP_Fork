package greencity.mapping;

import greencity.dto.notification.NotificationRequestDto;
import greencity.dto.notification.NotificationResponseDto;
import greencity.entity.Notification;
import greencity.enums.NotificationSection;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NotificationRequestDtoMapper extends AbstractConverter<NotificationRequestDto, Notification> {

    /**
     * Method for converting {@link NotificationRequestDto} into
     * {@link Notification}.
     *
     * @param notificationRequestDto object to convert.
     * @return converted object.
     * @author Mykhailo Derecha
     */
    @Override
    public Notification convert(NotificationRequestDto notificationRequestDto) {

        return Notification.builder()
            .message(notificationRequestDto.getMessage())
            .createdAt(LocalDateTime.now())
            .section(NotificationSection.valueOf(notificationRequestDto.getSection()))
            .description(notificationRequestDto.getDescription())
            .linkToFollow(notificationRequestDto.getLinkToFollow())
            .build();
    }
}
