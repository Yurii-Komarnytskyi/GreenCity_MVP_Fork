package greencity.repository;

import greencity.entity.*;
import greencity.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
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
public class ParticipationRepoTest {
    @Autowired
    private ParticipationRepo participationRepo;
    @Autowired
    private EventRepo eventRepo;
    @Autowired
    private UserRepo userRepo;

    private User user;
    private User user2;
    private User user3;
    private Event event;
    private Event event2;

    @BeforeEach
    void setUp() {
        participationRepo.deleteAll();
        userRepo.deleteAll();
        eventRepo.deleteAll();

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
        event2.setTitle("Sample Event2");
        event2.setDescription("Event description2");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setOpen(true);
        event2.setDuration(60);
        eventRepo.save(event2);
    }

    @Test
    void findAllTest() {
        ParticipationKey participationKey = new ParticipationKey(user, event);
        ParticipationKey participationKey2 = new ParticipationKey(user2, event);

        Participation participation = new Participation();
        participation.setId(participationKey);
        Participation participation2 = new Participation();
        participation2.setId(participationKey2);

        participationRepo.save(participation);
        participationRepo.save(participation2);

        List<Participation> result = participationRepo.findAll();
        assertEquals(2, result.size());
    }

    @Test
    void deleteAllTest() {
        ParticipationKey participationKey = new ParticipationKey(user, event);
        ParticipationKey participationKey2 = new ParticipationKey(user2, event);
        List<Participation> result;

        Participation participation = new Participation();
        participation.setId(participationKey);
        Participation participation2 = new Participation();
        participation2.setId(participationKey2);

        participationRepo.save(participation);
        participationRepo.save(participation2);
        result = participationRepo.findAll();
        assertEquals(2, result.size());

        participationRepo.deleteAll();
        result = participationRepo.findAll();
        assertEquals(0, result.size());
    }

    @Test
    void findUsersByEventIdTest() {
        long eventId = event.getId();

        ParticipationKey participationKey = new ParticipationKey(user, event);
        ParticipationKey participationKey2 = new ParticipationKey(user2, event);

        Participation participation = new Participation();
        participation.setId(participationKey);
        Participation participation2 = new Participation();
        participation2.setId(participationKey2);

        participationRepo.save(participation);
        participationRepo.save(participation2);

        List<User> result = participationRepo.findUsersByEventId(eventId);

        assertEquals(2, result.size());
    }

    @Test
    void findUsersByEventIdNoUsersTest() {
        long eventId = event2.getId();

        ParticipationKey participationKey = new ParticipationKey(user, event);
        ParticipationKey participationKey2 = new ParticipationKey(user2, event);

        Participation participation = new Participation();
        participation.setId(participationKey);
        Participation participation2 = new Participation();
        participation2.setId(participationKey2);

        participationRepo.save(participation);
        participationRepo.save(participation2);

        List<User> result = participationRepo.findUsersByEventId(eventId);

        assertEquals(0, result.size());
    }

    @Test
    void findEventsByUserIdTest() {
        long userId = user.getId();

        ParticipationKey participationKey = new ParticipationKey(user, event);
        ParticipationKey participationKey2 = new ParticipationKey(user2, event);
        ParticipationKey participationKey3 = new ParticipationKey(user, event2);

        Participation participation = new Participation();
        participation.setId(participationKey);
        Participation participation2 = new Participation();
        participation2.setId(participationKey2);
        Participation participation3 = new Participation();
        participation3.setId(participationKey3);

        participationRepo.save(participation);
        participationRepo.save(participation2);
        participationRepo.save(participation3);

        List<Event> result = participationRepo.findEventsByUserId(userId);

        assertEquals(2, result.size());
    }

    @Test
    void findEventsByUserIdNoEventsTest() {
        long userId = user3.getId();

        ParticipationKey participationKey = new ParticipationKey(user, event);
        ParticipationKey participationKey2 = new ParticipationKey(user2, event);
        ParticipationKey participationKey3 = new ParticipationKey(user, event2);

        Participation participation = new Participation();
        participation.setId(participationKey);
        Participation participation2 = new Participation();
        participation2.setId(participationKey2);
        Participation participation3 = new Participation();
        participation3.setId(participationKey3);

        participationRepo.save(participation);
        participationRepo.save(participation2);
        participationRepo.save(participation3);

        List<Event> result = participationRepo.findEventsByUserId(userId);

        assertEquals(0, result.size());
    }
}
