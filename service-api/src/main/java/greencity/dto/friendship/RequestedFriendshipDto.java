package greencity.dto.friendship;

import jakarta.validation.constraints.Min;

import java.util.Objects;

public class RequestedFriendshipDto {

    @Min(1)
    private Long senderId;

    @Min(1)
    private Long recipientId;

    public RequestedFriendshipDto() {
    }

    public RequestedFriendshipDto(Long senderId, Long recipientId) {
        this.senderId = senderId;
        this.recipientId = recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RequestedFriendshipDto that))
            return false;
        return Objects.equals(senderId, that.senderId) && Objects.equals(recipientId, that.recipientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(senderId, recipientId);
    }

    @Override
    public String toString() {
        return "FriendshipRequestDto{" +
            "requesterId=" + senderId +
            ", recipientId=" + recipientId +
            '}';
    }
}
