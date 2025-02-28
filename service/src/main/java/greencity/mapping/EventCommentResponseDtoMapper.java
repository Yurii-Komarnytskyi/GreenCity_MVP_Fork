package greencity.mapping;

import greencity.dto.event.EventCommentResponseDto;
import greencity.entity.EventComment;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EventCommentResponseDtoMapper extends AbstractConverter<EventComment, EventCommentResponseDto> {
    UserProfilePictureDtoMapper mapper = new UserProfilePictureDtoMapper();

    @Override
    protected EventCommentResponseDto convert(EventComment eventComment) {
        if (eventComment == null) {
            return null;
        }

        return EventCommentResponseDto.builder()
            .id(eventComment.getId())
            .text(eventComment.getText())
            .createdDate(eventComment.getCreatedDate())
            .modifiedDate(eventComment.getModifiedDate())
            .author(mapper.convert(eventComment.getUser()))
            .parentCommentId(eventComment.getParentComment() != null ? eventComment.getParentComment().getId() : null)
            .build();
    }
}
