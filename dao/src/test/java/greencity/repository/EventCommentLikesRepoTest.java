package greencity.repository;

import greencity.entity.*;
import greencity.enums.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
public class EventCommentLikesRepoTest {
    @Autowired
    EventCommentLikesRepo eventCommentLikesRepo;
    @Autowired
    UserRepo userRepo;
    @Autowired
    EventCommentRepo eventCommentRepo;
    @Autowired
    EventRepo eventRepo;

    private User user;
    private User user2;
    private EventComment eventComment;
    private EventComment eventComment2;
    private EventCommentLikes eventCommentLikes;
    private EventCommentLikes eventCommentLikes2;

    @BeforeEach
    void setUp() {
        eventCommentLikesRepo.deleteAll();
        userRepo.deleteAll();
        eventRepo.deleteAll();
        eventCommentRepo.deleteAll();

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

        Event event = new Event();
        event.setAuthor(user);
        event.setTitle("Sample Event");
        event.setDescription("Event description");
        event.setCreationDate(ZonedDateTime.now());
        event.setOpen(true);
        event.setDuration(60);
        eventRepo.save(event);

        eventComment = new EventComment();
        eventComment.setText("Event comment");
        eventComment.setUser(user);
        eventComment.setCreatedDate(LocalDateTime.now());
        eventCommentRepo.save(eventComment);

        eventComment2 = new EventComment();
        eventComment2.setText("Event comment2");
        eventComment2.setUser(user2);
        eventComment2.setCreatedDate(LocalDateTime.now());
        eventCommentRepo.save(eventComment2);
    }

    @Test
    void findAllTest() {
        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user2, eventComment2);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);

        List<EventCommentLikes> result = eventCommentLikesRepo.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void deleteAllTest() {
        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user2, eventComment2);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);

        List<EventCommentLikes> result;

        result = eventCommentLikesRepo.findAll();
        assertEquals(2, result.size());

        eventCommentLikesRepo.deleteAll();
        result = eventCommentLikesRepo.findAll();
        assertEquals(0, result.size());
    }

    @Test
    void findUsersByEventCommentIdTest() {
        long eventCommentId = eventComment.getId();

        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user2, eventComment);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);

        List<User> result = eventCommentLikesRepo.findUsersByEventCommentId(eventCommentId);
        assertEquals(2, result.size());
    }

    @Test
    void findUsersByEventCommentIdNoUsersTest() {
        long eventCommentId = eventComment2.getId();

        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user2, eventComment);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);

        List<User> result = eventCommentLikesRepo.findUsersByEventCommentId(eventCommentId);
        assertEquals(0, result.size());
    }

    @Test
    void findEventCommentsByUserIdTest() {
        long userId = user.getId();

        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user, eventComment2);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);

        List<EventComment> result = eventCommentLikesRepo.findEventCommentsByUserId(userId);
        assertEquals(2, result.size());
    }

    @Test
    void findEventCommentsByUserIdNoEventCommentsTest() {
        long userId = user2.getId();

        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user, eventComment2);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);

        List<EventComment> result = eventCommentLikesRepo.findEventCommentsByUserId(userId);
        assertEquals(0, result.size());
    }

    @Test
    void countLikesByEventCommentIdTest() {
        long eventCommentId = eventComment.getId();

        EventCommentLikesKey eventCommentLikesKey = new EventCommentLikesKey(user, eventComment);
        eventCommentLikes = new EventCommentLikes(eventCommentLikesKey, true, false);
        EventCommentLikesKey eventCommentLikesKey2 = new EventCommentLikesKey(user, eventComment2);
        eventCommentLikes2 = new EventCommentLikes(eventCommentLikesKey2, true, false);
        EventCommentLikesKey eventCommentLikesKey3 = new EventCommentLikesKey(user2, eventComment);
        EventCommentLikes eventCommentLikes3 = new EventCommentLikes(eventCommentLikesKey3, true, false);

        eventCommentLikesRepo.save(eventCommentLikes);
        eventCommentLikesRepo.save(eventCommentLikes2);
        eventCommentLikesRepo.save(eventCommentLikes3);

        long result = eventCommentLikesRepo.countLikesByEventCommentId(eventCommentId);

        Assertions.assertEquals(2, result);
    }
}
