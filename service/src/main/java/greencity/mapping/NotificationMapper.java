package greencity.mapping;

import greencity.dto.notification.NotificationResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.entity.Notification;
import greencity.entity.UserShoppingListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class NotificationMapper extends
    AbstractConverter<Notification, NotificationResponseDto> {

    /**
     * Method for converting {@link Notification} into
     * {@link NotificationResponseDto}.
     *
     * @param notification object to convert.
     * @return converted object.
     * @author Mykhailo Derecha
     */
    @Override
    public NotificationResponseDto convert(Notification notification) {

        return NotificationResponseDto.builder()
            .id(notification.getId())
            .createdAt(notification.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .linkToFollow(notification.getLinkToFollow())
            .isViewed(notification.getViewedAt() != null)
            .description(notification.getDescription())
            .message(notification.getMessage())
            .senderId(notification.getSender().getId())
            .receiverId(notification.getReceiver().getId())
            .section(String.valueOf(notification.getSection()))
            .build();
    }
}
