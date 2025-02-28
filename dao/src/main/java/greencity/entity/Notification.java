package greencity.entity;

import greencity.enums.NotificationSection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@EqualsAndHashCode(
    exclude = {"sender", "receiver"})
@ToString(
    exclude = {"sender", "receiver"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String linkToFollow;

    private String message;

    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime viewedAt;

    @Enumerated(EnumType.STRING)
    private NotificationSection section = NotificationSection.GreenCity;
}
