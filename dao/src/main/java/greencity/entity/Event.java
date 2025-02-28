package greencity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "event")
@ToString(exclude = {"author"})
@EqualsAndHashCode(exclude = {"author"})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private ZonedDateTime creationDate;

    @ManyToOne
    private User author;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<EventComment> comments = new ArrayList<>();

    @Column(nullable = false)
    private int duration;

    @Transient
    private int likes;

    @Column(name = "rating")
    private double rating;

    @ManyToMany
    @JoinTable(
        name = "event_initiative_type",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "initiative_type_id"))
    private List<InitiativeType> initiativeTypes = new ArrayList<>();

    @Column(name = "is_open")
    private boolean isOpen;

    @ManyToMany
    @JoinTable(
        name = "event_image",
        joinColumns = @JoinColumn(name = "event_id"),
        inverseJoinColumns = @JoinColumn(name = "image_id"))
    private Set<Image> images = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "main_image_id")
    private Image mainImage;
}
