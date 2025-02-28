package greencity.entity;

import greencity.enums.FriendshipStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "friendships")
public class Friendship {

    public Friendship() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "friendship_request_expiration")
    private LocalDateTime friendshipRequestExpiration;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
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
        if (!(o instanceof Friendship that))
            return false;
        return Objects.equals(id, that.id) && Objects.equals(user, that.user) && Objects.equals(friend, that.friend)
            && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, friend, status);
    }

    @Override
    public String toString() {
        return "Friendship{" +
            "id=" + id +
            ", user=" + user +
            ", friend=" + friend +
            ", status=" + status +
            ", requestedAt=" + requestedAt +
            ", friendshipRequestExpiration=" + friendshipRequestExpiration +
            '}';
    }
}