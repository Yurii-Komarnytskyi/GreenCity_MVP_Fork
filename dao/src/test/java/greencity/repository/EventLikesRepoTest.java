package greencity.repository;

import greencity.entity.Event;
import greencity.entity.EventLikes;
import greencity.entity.EventLikesKey;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class EventLikesRepoTest {
    @Autowired
    private EventLikesRepo eventLikesRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    EventRepo eventRepo;

    private User user;
    private User user2;
    private User user3;
    private Event event;
    private Event event2;
    private EventLikes eventLikes;
    private EventLikes eventLikes2;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        eventRepo.deleteAll();
        eventLikesRepo.deleteAll();

        user = new User();
        user.setFirstName("John");
        user.setEmail("john.doe@mail.com");
        user.setDateOfRegistration(LocalDateTime.now());
        user.setName("John Doe");
        user.setRefreshTokenKey("token");
        user.setRole(Role.ROLE_USER);
        userRepo.save(user);

        user2 = new User();
        user2.setFirstName("Jane");
        user2.setEmail("jane.doe@mail.com");
        user2.setDateOfRegistration(LocalDateTime.now());
        user2.setName("Jane Doe");
        user2.setRefreshTokenKey("token2");
        user2.setRole(Role.ROLE_USER);
        userRepo.save(user2);

        user3 = new User();
        user3.setFirstName("Ann");
        user3.setEmail("ann.doe@mail.com");
        user3.setDateOfRegistration(LocalDateTime.now());
        user3.setName("Ann Doe");
        user3.setRefreshTokenKey("token3");
        user3.setRole(Role.ROLE_USER);
        userRepo.save(user3);

        event = new Event();
        event.setAuthor(user);
        event.setTitle("Sample Event");
        event.setDescription("Event description");
        event.setCreationDate(ZonedDateTime.now());
        event.setOpen(true);
        event.setDuration(60);
        eventRepo.save(event);

        event2 = new Event();
        event2.setAuthor(user);
        event2.setTitle("Sample Event");
        event2.setDescription("Event description");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setOpen(true);
        event2.setDuration(60);
        eventRepo.save(event2);
    }

    @Test
    void findAllTest() {
        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user3, event);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        List<EventLikes> result = eventLikesRepo.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void deleteAllTest() {
        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user3, event);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        List<EventLikes> result;

        result = eventLikesRepo.findAll();
        assertEquals(2, result.size());

        eventLikesRepo.deleteAll();
        result = eventLikesRepo.findAll();
        assertEquals(0, result.size());
    }

    @Test
    void findUsersByEventIdTest() {
        long eventId = event.getId();

        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user3, event);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        List<User> result = eventLikesRepo.findUsersByEventId(eventId);
        assertEquals(2, result.size());
    }

    @Test
    void findUsersByEventIdNoUsersTest() {
        long eventId = event2.getId();

        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user3, event);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        List<User> result = eventLikesRepo.findUsersByEventId(eventId);
        assertEquals(0, result.size());
    }

    @Test
    void findEventsByUserIdTest() {
        long userId = user2.getId();

        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user2, event2);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        List<Event> result = eventLikesRepo.findEventsByUserId(userId);
        assertEquals(2, result.size());
    }

    @Test
    void findEventsByUserIdNoEventsTest() {
        long userId = user.getId();

        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user2, event2);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        List<Event> result = eventLikesRepo.findEventsByUserId(userId);
        assertEquals(0, result.size());
    }

    @Test
    void countLikesByEventIdTest() {
        long eventId = event.getId();

        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user3, event);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        long likes = eventLikesRepo.countLikesByEventId(eventId);

        assertEquals(2, likes);
    }

    @Test
    void countLikesByEventIdNoLikesTest() {
        long eventId = event2.getId();

        EventLikesKey eventLikesKey = new EventLikesKey(user2, event);
        eventLikes = new EventLikes(eventLikesKey, true, false);
        EventLikesKey eventLikesKey2 = new EventLikesKey(user3, event);
        eventLikes2 = new EventLikes(eventLikesKey2, true, false);

        eventLikesRepo.save(eventLikes);
        eventLikesRepo.save(eventLikes2);

        long likes = eventLikesRepo.countLikesByEventId(eventId);

        assertEquals(0, likes);
    }
}
