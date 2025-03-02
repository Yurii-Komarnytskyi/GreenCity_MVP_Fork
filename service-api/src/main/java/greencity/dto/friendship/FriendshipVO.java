package greencity.dto.friendship;

import greencity.dto.user.UserVO;
import greencity.enums.FriendshipStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

public class FriendshipVO {

    @Min(0)
    @NotNull
    private Long id;

    @NotNull
    private UserVO user;

    @NotNull
    private UserVO friend;

    @NotNull
    private FriendshipStatus status;

    private LocalDateTime requestedAt;

    private LocalDateTime friendshipRequestExpiration;

    public FriendshipVO() {}

    public FriendshipVO(
            Long id,
            UserVO user,
            UserVO friend,
            FriendshipStatus status,
            LocalDateTime requestedAt,
            LocalDateTime friendshipRequestExpiration) {
        this.id = id;
        this.user = user;
        this.friend = friend;
        this.status = status;
        this.requestedAt = requestedAt;
        this.friendshipRequestExpiration = friendshipRequestExpiration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserVO getUser() {
        return user;
    }

    public void setUser(UserVO user) {
        this.user = user;
    }

    public UserVO getFriend() {
        return friend;
    }

    public void setFriend(UserVO friend) {
        this.friend = friend;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getFriendshipRequestExpiration() {
        return friendshipRequestExpiration;
    }

    public void setFriendshipRequestExpiration(LocalDateTime friendshipRequestExpiration) {
        this.friendshipRequestExpiration = friendshipRequestExpiration;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FriendshipVO that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(friend, that.friend) && status == that.status && Objects.equals(requestedAt, that.requestedAt) && Objects.equals(friendshipRequestExpiration, that.friendshipRequestExpiration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, friend, status, requestedAt, friendshipRequestExpiration);
    }

    @Override
    public String toString() {
        return "FriendshipVO{" +
                "id=" + id +
                ", user=" + user +
                ", friend=" + friend +
                ", status=" + status +
                ", requestedAt=" + requestedAt +
                ", friendshipRequestExpiration=" + friendshipRequestExpiration +
                '}';
    }
}
