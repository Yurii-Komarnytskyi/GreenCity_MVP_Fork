package greencity.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Table(name = "user_achievements")
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "achievement_id")
    private Achievement achievement;

    @CreationTimestamp
    private LocalDateTime achievementDate;

    @Column(nullable = false, columnDefinition = "boolean default false")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isActive = false;

}
