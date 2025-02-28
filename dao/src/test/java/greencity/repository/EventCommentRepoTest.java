package greencity.repository;

import greencity.entity.Event;
import greencity.entity.EventComment;
import greencity.entity.User;
import greencity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

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
public class EventCommentRepoTest {
    @Autowired
    UserRepo userRepo;
    @Autowired
    EventCommentRepo eventCommentRepo;
    @Autowired
    EventRepo eventRepo;

    private User user;
    private Event event;
    private EventComment eventComment;
    private User user2;

    @BeforeEach
    void setUp() {
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

        event = new Event();
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
        eventComment.setEvent(event);

        EventComment eventComment2 = new EventComment();
        eventComment2.setText("Event comment2");
        eventComment2.setUser(user2);
        eventComment2.setCreatedDate(LocalDateTime.now());
        eventComment2.setEvent(event);

        eventCommentRepo.save(eventComment);
        eventCommentRepo.save(eventComment2);
    }

    @Test
    void findByIdTest() {
        Optional<EventComment> result = eventCommentRepo.findById(eventComment.getId());

        assertTrue(result.isPresent());
        assertTrue(result.get().getUser().equals(user));
    }

    @Test
    void findByEventWithPaginationTest() {
        Page<EventComment> result = eventCommentRepo.findByEvent(event, PageRequest.of(0, 10));
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findByIdNoSuchIdTest() {
        Optional<EventComment> result = eventCommentRepo.findById(77L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEventNoSuchEventTest() {
        Event nonexistentEvent = new Event();
        nonexistentEvent.setAuthor(user);
        nonexistentEvent.setTitle("New Event");
        nonexistentEvent.setCreationDate(ZonedDateTime.now());
        nonexistentEvent.setId(9L);
        Page<EventComment> result = eventCommentRepo.findByEvent(nonexistentEvent, PageRequest.of(0, 10));
        assertEquals(0, result.getContent().size());
    }

    @Test
    void findTopLevelCommentsByEventTest() {
        List<EventComment> result = eventCommentRepo.findTopLevelCommentsByEvent(event);

        assertEquals(2, result.size());
    }

    @Test
    void findByUserTest() {
        List<EventComment> result = eventCommentRepo.findByUser(user);

        assertEquals(1, result.size());
        assertEquals("Event comment", result.getFirst().getText());
    }

    @Test
    void findByNonexistentUserTest() {
        User nonexistentUser = new User();
        nonexistentUser.setFirstName("Joe");
        nonexistentUser.setEmail("joe.doe@mail.com");
        nonexistentUser.setDateOfRegistration(LocalDateTime.now());
        nonexistentUser.setName("Joe Doe");
        nonexistentUser.setRefreshTokenKey("token7");
        nonexistentUser.setRole(Role.ROLE_USER);
        nonexistentUser.setId(77L);

        List<EventComment> result = eventCommentRepo.findByUser(nonexistentUser);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByParentCommentTest() {
        EventComment eventComment3 = new EventComment();
        eventComment3.setText("Event comment3");
        eventComment3.setUser(user2);
        eventComment3.setCreatedDate(LocalDateTime.now());
        eventComment3.setEvent(event);
        eventComment3.setParentComment(eventComment);

        eventCommentRepo.save(eventComment3);

        List<EventComment> result = eventCommentRepo.findByParentComment(eventComment);

        assertEquals(1, result.size());
        assertEquals("Event comment3", result.getFirst().getText());
    }

    @Test
    void findByParentCommentNoParentCommentsTest() {
        List<EventComment> result = eventCommentRepo.findByParentComment(eventComment);

        assertTrue(result.isEmpty());
    }

    @Test
    void findNonDeletedCommentsByEventTest() {
        EventComment eventComment3 = new EventComment();
        eventComment3.setText("Event comment3");
        eventComment3.setUser(user2);
        eventComment3.setCreatedDate(LocalDateTime.now());
        eventComment3.setEvent(event);
        eventComment3.setParentComment(eventComment);
        eventComment3.setDeleted(true);

        eventCommentRepo.save(eventComment3);

        List<EventComment> result = eventCommentRepo.findNonDeletedCommentsByEvent(event);
        assertEquals(2, result.size());
    }

    @Test
    void countByEventTest() {
        long result = eventCommentRepo.countByEvent(event);

        assertEquals(2, result);
    }

    @Test
    void countByParentCommentTest() {
        long result = eventCommentRepo.countRepliesByParentComment(eventComment);

        assertEquals(0, result);

        EventComment eventComment3 = new EventComment();
        eventComment3.setText("Event comment3");
        eventComment3.setUser(user2);
        eventComment3.setCreatedDate(LocalDateTime.now());
        eventComment3.setEvent(event);
        eventComment3.setParentComment(eventComment);

        eventCommentRepo.save(eventComment3);

        result = eventCommentRepo.countRepliesByParentComment(eventComment);
        assertEquals(1, result);
    }
}
