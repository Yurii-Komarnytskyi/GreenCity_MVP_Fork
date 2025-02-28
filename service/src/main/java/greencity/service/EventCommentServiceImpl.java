package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.event.AddEventCommentDtoResponse;
import greencity.dto.event.EventCommentRequestDto;
import greencity.dto.event.EventCommentResponseDto;
import greencity.dto.notification.NotificationRequestDto;
import greencity.dto.user.UserVO;
import greencity.entity.Event;
import greencity.entity.EventComment;
import greencity.entity.User;
import greencity.enums.NotificationSection;
import greencity.repository.EventCommentRepo;
import greencity.repository.EventRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class EventCommentServiceImpl implements EventCommentService {
    private final EventRepo eventRepo;
    private final EventCommentRepo eventCommentRepo;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    public AddEventCommentDtoResponse addComment(Long eventId, Long userId, EventCommentRequestDto requestDto) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        UserVO userVO = userService.findById(userId);
        if (userVO == null) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }

        User user = modelMapper.map(userVO, User.class);

        EventComment eventComment = EventComment.builder()
            .text(requestDto.getText())
            .user(user)
            .event(event)
            .createdDate(LocalDateTime.now())
            .modifiedDate(LocalDateTime.now())
            .deleted(false)
            .build();

        eventComment = eventCommentRepo.save(eventComment);

        if (!event.getAuthor().getId().equals(user.getId())) {
            sendCommentNotificationEmail(event.getAuthor(), event, eventComment);

            String notificationMessage = String.format(
                "<img src='https://greencity.com/logo.png' alt='GreenCity Logo'/>" +
                    "<p>Hi %s,</p>" +
                    "<p>You've got a new comment on your event <b>%s</b>.</p>" +
                    "<p><b>Comment by:</b> %s (%s)</p>" +
                    "<p><b>Comment:</b> %s</p>" +
                    "<a href='%s' style='display:inline-block;padding:10px 20px;background:#4CAF50;color:white;text-decoration:none;'>Go to comment</a>"
                    +
                    "<p>Sincerely yours,<br>GreenCity team</p>",
                event.getAuthor().getName(),
                event.getTitle(),
                user.getName(),
                eventComment.getCreatedDate(),
                requestDto.getText(),
                "/events/" + eventId);

            String plainMessage = Jsoup.parse(notificationMessage).text();

            notificationService.addNotification(new NotificationRequestDto(
                user.getId(),
                event.getAuthor().getId(),
                "New comment on your event: " + event.getTitle(),
                plainMessage,
                NotificationSection.GreenCity.name(),
                "/events/" + eventId));
        }

        List<User> mentionedUsers = extractMentionedUsers(requestDto.getText());
        for (User mentionedUser : mentionedUsers) {
            if (!mentionedUser.getId().equals(user.getId())) {
                notificationService.addNotification(new NotificationRequestDto(
                    user.getId(),
                    mentionedUser.getId(),
                    "You were mentioned in a comment on: " + event.getTitle(),
                    String.format("User %s mentioned you in a comment: \"%s\".", user.getName(), requestDto.getText()),
                    NotificationSection.GreenCity.name(),
                    "/events/" + eventId));
            }
        }

        return AddEventCommentDtoResponse.builder()
            .id(eventComment.getId())
            .text(eventComment.getText())
            .createdDate(eventComment.getCreatedDate())
            .modifiedDate(eventComment.getModifiedDate())
            .build();
    }

    private List<User> extractMentionedUsers(String text) {
        List<User> mentionedUsers = new ArrayList<>();

        Pattern pattern = Pattern.compile("[@#]([\\w_]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String taggedName = matcher.group(1).trim();

            String fullName = taggedName.replace("_", " ");

            Optional<UserVO> mentionedUserVO = userService.findByFullName(fullName);

            if (mentionedUserVO.isPresent()) {
                mentionedUsers.add(modelMapper.map(mentionedUserVO.get(), User.class));
            } else {
                throw new EntityNotFoundException("User with name " + fullName + " not found.");
            }
        }
        return mentionedUsers;
    }

    @Override
    public AddEventCommentDtoResponse replyToComment(Long parentCommentId, Long userId,
        EventCommentRequestDto requestDto) {
        EventComment parentComment = eventCommentRepo.findById(parentCommentId)
            .orElseThrow(() -> new EntityNotFoundException("Parent comment not found with id: " + parentCommentId));

        Event event = parentComment.getEvent();

        UserVO userVO = userService.findById(userId);
        if (userVO == null) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        User user = modelMapper.map(userVO, User.class);

        EventComment replyComment = EventComment.builder()
            .text(requestDto.getText())
            .user(user)
            .event(event)
            .parentComment(parentComment)
            .createdDate(LocalDateTime.now())
            .modifiedDate(LocalDateTime.now())
            .deleted(false)
            .build();

        replyComment = eventCommentRepo.save(replyComment);

        if (!parentComment.getUser().equals(user)) {
            notificationService.addNotification(new NotificationRequestDto(
                user.getId(),
                parentComment.getUser().getId(),
                "Someone replied to your comment on: " + event.getTitle(),
                "You received a reply to your comment.",
                NotificationSection.GreenCity.name(),
                "/events/" + event.getId()));
        }

        return AddEventCommentDtoResponse.builder()
            .id(replyComment.getId())
            .text(replyComment.getText())
            .createdDate(replyComment.getCreatedDate())
            .modifiedDate(replyComment.getModifiedDate())
            .build();
    }

    @Override
    public void deleteComment(Long commentId) {
        // This method is yet to be implemented
    }

    @Override
    public EventCommentResponseDto updateComment(Long commentId, EventCommentRequestDto requestDto) {
        // This method is yet to be implemented
        return null;
    }

    @Override
    public PageableAdvancedDto<EventCommentResponseDto> getCommentsByEvent(Long eventId, int page, int size) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        Pageable pageable = PageRequest.of(page, size);
        Page<EventComment> eventComments = eventCommentRepo.findByEventOrderByCreatedDateDesc(event, pageable);

        List<EventCommentResponseDto> content = eventComments.getContent().stream()
            .map(comment -> modelMapper.map(comment, EventCommentResponseDto.class))
            .toList();

        return new PageableAdvancedDto<>(
            content,
            eventComments.getTotalElements(),
            eventComments.getNumber(),
            eventComments.getTotalPages(),
            eventComments.getSize(),
            eventComments.hasPrevious(),
            eventComments.hasNext(),
            eventComments.isFirst(),
            eventComments.isLast());
    }

    @Override
    public EventCommentResponseDto getCommentById(Long eventId, Long commentId) {
        EventComment comment = eventCommentRepo.findById(commentId)
            .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new EntityNotFoundException("Comment with id " + commentId + " does not belong to event " + eventId);
        }

        return modelMapper.map(comment, EventCommentResponseDto.class);
    }

    @Override
    public List<EventCommentResponseDto> getRepliesByComment(Long commentId) {
        // This method is yet to be implemented
        return List.of();
    }

    @Override
    public long countCommentsByEvent(Long eventId) {
        Event event = eventRepo.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found with id: " + eventId));

        return eventCommentRepo.countByEvent(event);
    }

    @Override
    public long countRepliesByComment(Long commentId) {
        // This method is yet to be implemented
        return 0;
    }

    private void sendCommentNotificationEmail(User organizer, Event event, EventComment comment) {
        String emailBody = String.format(
            "<p>Hi %s,</p>" +
                "<p>You've got a new comment on your event <b>%s</b>.</p>" +
                "<p><b>Comment by:</b> %s (%s)</p>" +
                "<p><b>Comment:</b> %s</p>" +
                "<a href='%s' style='display:inline-block;padding:10px 20px;background:#4CAF50;color:white;text-decoration:none;'>Go to comment</a>"
                +
                "<p>Sincerely yours,<br>GreenCity team</p>",
            organizer.getName(),
            event.getTitle(),
            comment.getUser().getName(),
            comment.getCreatedDate(),
            comment.getText(),
            "/events/" + event.getId());

        String emailSubject = "📢 New Comment on Your Event: " + event.getTitle();

        try {
            emailService.sendEmail(organizer.getEmail(), emailSubject, emailBody);
        } catch (Exception e) {
            log.error("Failed to send comment notification email to organizer: {}", organizer.getEmail(), e);
        }
    }
}
