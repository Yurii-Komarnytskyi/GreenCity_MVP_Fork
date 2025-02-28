package greencity.service;

import greencity.dto.notification.NotificationRequestDto;
import greencity.dto.notification.NotificationResponseDto;
import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.NotificationSection;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.mapping.NotificationMapper;
import greencity.mapping.NotificationRequestDtoMapper;
import greencity.repository.NotificationRepo;
import greencity.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;
    private final NotificationMapper notificationMapper;
    private final NotificationRequestDtoMapper notificationRequestDtoMapper;
    private final UserRepo userRepo;

    /**
     * Method to create a new Notification by passing {@link NotificationRequestDto}
     * as an input parameter
     *
     * @param newNotification as a DTO to create a new notification
     *
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    @Override
    @Transactional
    public NotificationResponseDto addNotification(NotificationRequestDto newNotification) {

        User sender = findUserById(newNotification.getSenderId(), "sender");
        User receiver = findUserById(newNotification.getReceiverId(), "receiver");

        if (sender.equals(receiver))
            throw new BadRequestException("Sender and receiver can't be the same user!");

        Notification createdNotification = notificationRequestDtoMapper.convert(newNotification);

        createdNotification.setSender(sender);
        createdNotification.setReceiver(receiver);

        createdNotification = notificationRepo.save(createdNotification);

        return notificationMapper.convert(createdNotification);

    }

    /**
     * Method get all the notifications that have been sent by a sender
     *
     * @param senderId to get all notifications by sender as Pageable
     * @param pageable
     *
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Override
    public Page<NotificationResponseDto> getNotificationsBySender(Long senderId, Pageable pageable) {

        User sender = findUserById(senderId, "sender");
        Page<Notification> notificationsBySender = notificationRepo.findBySender(sender, pageable);

        return notificationsBySender.map(notificationMapper::convert);

    }

    /**
     * Method get all the notifications that have been received by a receiver
     *
     * @param receiverId to get all notifications by receiverId as Pageable
     * @param pageable
     *
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Override
    public Page<NotificationResponseDto> getNotificationsByReceiver(Long receiverId, Pageable pageable) {

        User receiver = findUserById(receiverId, "receiver");
        Page<Notification> notificationsByReceiver = notificationRepo.findByReceiver(receiver, pageable);

        return notificationsByReceiver.map(notificationMapper::convert);
    }

    /**
     * Method get all the notifications whether being viewed or not
     *
     * @param viewedStatus to get all notifications by viewedStatus
     * @param pageable
     *
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Override
    public Page<NotificationResponseDto> getNotificationsByViewedStatus(Boolean viewedStatus, Pageable pageable) {

        List<Notification> notificationsByViewedStatus = notificationRepo.findAll(pageable).stream()
            .filter(not -> (not.getViewedAt() == null) == viewedStatus).toList();

        List<NotificationResponseDto> notificationResponseDtos = notificationsByViewedStatus.stream()
            .map(notificationMapper::convert).toList();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), notificationResponseDtos.size());

        notificationResponseDtos = notificationResponseDtos.subList(start, end);

        return new PageImpl<>(notificationResponseDtos, pageable, notificationResponseDtos.size());

    }

    /**
     * Method get all the notifications by section
     *
     * @param section  to get all notifications by viewedStatus
     * @param pageable
     *
     * @return {@link Page<NotificationResponseDto>}.
     * @author Mykhailo Derecha
     */
    @Override
    public Page<NotificationResponseDto> getNotificationsBySection(String section, Pageable pageable) {

        Page<Notification> notificationsBySection = notificationRepo.findBySection(NotificationSection.valueOf(section),
            pageable);

        return notificationsBySection.map(notificationMapper::convert);
    }

    /**
     * Method that marks all the notifications of some particular receiver as read
     *
     * @param receiverId
     *
     * @return {@link boolean}.
     * @author Mykhailo Derecha
     */
    @Override
    @Transactional
    public boolean markAllNotViewedAsRead(Long receiverId) {

        User receiver = findUserById(receiverId, "receiver");

        List<Notification> notificationsToMark = notificationRepo.findByReceiver(receiver);

        return notificationsToMark.stream().peek(not -> {
            not.setViewedAt(LocalDateTime.now());
            notificationRepo.save(not);
        })
            .noneMatch(not -> not.getViewedAt() == null);
    }

    /**
     * Method that marks some particular notification as read
     *
     * @param notificationId
     *
     * @return {@link boolean}.
     * @author Mykhailo Derecha
     */
    @Override
    @Transactional
    public boolean markAsRead(Long notificationId) {

        Notification not = notificationRepo.findById(notificationId).orElseThrow(
            () -> new NotFoundException("Such a notification does not exist! "));
        not.setViewedAt(LocalDateTime.now());
        not = notificationRepo.save(not);

        return not.getViewedAt() != null;
    }

    /**
     * Method that allows to get some notification by id
     *
     * @param notificationId
     *
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    @Override
    public NotificationResponseDto getNotificationById(Long notificationId) {

        Notification not = notificationRepo.findById(notificationId).orElseThrow(
            () -> new NotFoundException("Such a notification does not exist! "));

        return notificationMapper.convert(not);
    }

    /**
     * Private method that finds a user by id
     *
     * @param id
     * @param userType - it can be a sender or receiver, used in the exception
     *                 message
     *
     * @return {@link NotificationResponseDto}.
     * @author Mykhailo Derecha
     */
    private User findUserById(Long id, String userType) {

        return userRepo.findById(id).orElseThrow(
            () -> new NotFoundException("Such a " + userType + " does not exist!"));

    }
}
