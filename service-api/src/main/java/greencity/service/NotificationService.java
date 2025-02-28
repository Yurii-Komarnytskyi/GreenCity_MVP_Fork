package greencity.service;

import greencity.dto.notification.NotificationRequestDto;
import greencity.dto.notification.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Method to create a new Notification by passing {@link NotificationRequestDto}
     * as an input parameter
     *
     * @param newNotification as a DTO to create a new notification
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    NotificationResponseDto addNotification(NotificationRequestDto newNotification);

    /**
     * Method get all the notifications that have been sent by some sender
     *
     * @param senderId to get all notifications by sender as Pageable
     * @param pageable
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    Page<NotificationResponseDto> getNotificationsBySender(Long senderId, Pageable pageable);

    /**
     * Method get all the notifications that have been received by some receiver
     *
     * @param receiverId to get all notifications by receiverId as Pageable
     * @param pageable
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    Page<NotificationResponseDto> getNotificationsByReceiver(Long receiverId, Pageable pageable);

    /**
     * Method get all the notifications whether being viewed or not
     *
     * @param viewedStatus to get all notifications by viewedStatus
     * @param pageable
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    Page<NotificationResponseDto> getNotificationsByViewedStatus(Boolean viewedStatus, Pageable pageable);

    /**
     * Method get all the notifications by section
     *
     * @param section  to get all notifications by viewedStatus
     * @param pageable
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    Page<NotificationResponseDto> getNotificationsBySection(String section, Pageable pageable);

    /**
     * Method that marks all the notifications of some particular receiver as read
     *
     * @param receiverId
     * @return {@link boolean}.
     * @author Mykhailo Derecha
     */
    boolean markAllNotViewedAsRead(Long receiverId);

    /**
     * Method that marks some particular notification as read
     *
     * @param notificationId
     * @return {@link boolean}.
     * @author Mykhailo Derecha
     */
    boolean markAsRead(Long notificationId);

    /**
     * Method that allows to get some notification by id
     *
     * @param notificationId
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    NotificationResponseDto getNotificationById(Long notificationId);

}
