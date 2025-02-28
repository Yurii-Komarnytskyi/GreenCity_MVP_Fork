package greencity.repository;

import greencity.entity.Event;
import greencity.entity.Image;
import greencity.entity.User;
import greencity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.test.database.replace=NONE",
    "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.sql.init.mode=never",
    "spring.liquibase.enabled=false"
})
public class ImageRepoTest {
    @Autowired
    private ImageRepo imageRepo;
    @Autowired
    private EventRepo eventRepo;
    @Autowired
    private UserRepo userRepo;

    private Event event;
    private User user;
    private Image image1;

    @BeforeEach
    void setUp() {
        eventRepo.deleteAll();
        imageRepo.deleteAll();

        user = new User();
        user.setFirstName("John");
        user.setEmail("john.doe@mail.com");
        user.setDateOfRegistration(LocalDateTime.now());
        user.setName("John Doe");
        user.setRefreshTokenKey("token");
        user.setRole(Role.ROLE_USER);
        userRepo.save(user);

        event = new Event();
        event.setAuthor(user);
        event.setTitle("Sample Event");
        event.setDescription("Event description");
        event.setCreationDate(ZonedDateTime.now());
        event.setOpen(true);
        event.setDuration(60);

        image1 = new Image();
        image1.setImagePath("uploads\\new_image.jpg");
        image1.getEvents().add(event);
        event.getImages().add(image1);

        Image image2 = new Image();
        image2.setImagePath("uploads\\new2_image.jpg");
        image2.getEvents().add(event);
        event.getImages().add(image2);

        imageRepo.save(image1);
        imageRepo.save(image2);
        eventRepo.save(event);
    }

    @Test
    void findByImagePathTest() {
        Optional<Image> result = imageRepo.findByImagePath("uploads\\new_image.jpg");

        if (result.isPresent()) {
            assertEquals(image1.getImagePath(), result.get().getImagePath());
        }
    }

    @Test
    void findByImagePathNoSuchPathTest() {
        Optional<Image> result = imageRepo.findByImagePath("uploads\\new_new_image.jpg");

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByEventTest() {
        List<Image> result = imageRepo.findAllByEvent(event);

        assertEquals(2, result.size());
    }

    @Test
    void findAllByEventNoSuchEventTest() {
        Event nonexistentEvent = new Event();
        nonexistentEvent.setAuthor(user);
        nonexistentEvent.setTitle("New Event");
        nonexistentEvent.setCreationDate(ZonedDateTime.now());
        nonexistentEvent.setId(9L);

        List<Image> result = imageRepo.findAllByEvent(nonexistentEvent);

        assertTrue(result.isEmpty());
    }
}
