package greencity.repository;

import greencity.entity.Event;
import greencity.entity.EventDateInfo;
import greencity.enums.Role;
import greencity.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.test.database.replace=NONE",
    "spring.datasource.url=jdbc:h2:mem:test;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.sql.init.mode=never",
    "spring.liquibase.enabled=false"
})
public class EventDateInfoRepoTest {

    @Autowired
    private EventDateInfoRepo eventDateInfoRepo;

    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private UserRepo userRepo;

    private Event event;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        eventRepo.deleteAll();
        eventDateInfoRepo.deleteAll();

        User author = new User();
        author.setFirstName("John");
        author.setEmail("john.doe@mail.com");
        author.setDateOfRegistration(LocalDateTime.now());
        author.setName("John Doe");
        author.setRefreshTokenKey("token");
        author.setRole(Role.ROLE_USER);
        userRepo.save(author);

        event = new Event();
        event.setTitle("Sample Event");
        event.setDescription("Event Description");
        event.setCreationDate(ZonedDateTime.now());
        event.setAuthor(author);
        event.setOpen(true);
        event.setDuration(120);
        eventRepo.save(event);
    }

    @Test
    void testFindByEvent() {
        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .event(event)
            .eventDate(LocalDate.now())
            .isAllDay(true)
            .isPlace(true)
            .isOnline(false)
            .location("Kyiv")
            .url("https://example.com")
            .build();
        eventDateInfoRepo.save(eventDateInfo);

        List<EventDateInfo> foundEventDateInfos = eventDateInfoRepo.findByEvent(event);

        assertEquals(1, foundEventDateInfos.size());
        assertEquals(eventDateInfo.getId(), foundEventDateInfos.get(0).getId());
    }

    @Test
    void testFindByEventDate() {
        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .event(event)
            .eventDate(LocalDate.now())
            .isAllDay(false)
            .isPlace(false)
            .isOnline(true)
            .location("Lviv")
            .url("https://example.com/online")
            .build();
        eventDateInfoRepo.save(eventDateInfo);

        List<EventDateInfo> foundEventDateInfos = eventDateInfoRepo.findByEventDate(LocalDate.now());

        assertEquals(1, foundEventDateInfos.size());
        assertEquals(eventDateInfo.getEventDate(), foundEventDateInfos.get(0).getEventDate());
    }

    @Test
    void testFindByIsOnlineTrue() {
        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .event(event)
            .eventDate(LocalDate.now())
            .isAllDay(false)
            .isPlace(false)
            .isOnline(true)
            .location("Online")
            .url("https://online.com")
            .build();
        eventDateInfoRepo.save(eventDateInfo);

        List<EventDateInfo> foundEventDateInfos = eventDateInfoRepo.findByIsOnlineTrue();

        assertEquals(1, foundEventDateInfos.size());
        assertTrue(foundEventDateInfos.get(0).isOnline());
    }

    @Test
    void testFindByLocation() {
        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .event(event)
            .eventDate(LocalDate.now())
            .isAllDay(false)
            .isPlace(true)
            .isOnline(false)
            .location("Ivano-Frankivsk")
            .url("https://example.com/offline")
            .build();
        eventDateInfoRepo.save(eventDateInfo);

        List<EventDateInfo> foundEventDateInfos = eventDateInfoRepo.findByLocation("Ivano-Frankivsk");

        assertEquals(1, foundEventDateInfos.size());
        assertEquals("Ivano-Frankivsk", foundEventDateInfos.get(0).getLocation());
    }

    @Test
    void testFindByEventDateBetween() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .event(event)
            .eventDate(LocalDate.now())
            .isAllDay(false)
            .isPlace(true)
            .isOnline(false)
            .location("Kyiv")
            .url("https://example.com/range")
            .build();
        eventDateInfoRepo.save(eventDateInfo);

        List<EventDateInfo> foundEventDateInfos = eventDateInfoRepo.findByEventDateBetween(startDate, endDate);

        assertEquals(1, foundEventDateInfos.size());
        assertEquals(LocalDate.now(), foundEventDateInfos.get(0).getEventDate());
    }

    @Test
    void testFindByIsAllDayTrue() {
        EventDateInfo eventDateInfo = EventDateInfo.builder()
            .event(event)
            .eventDate(LocalDate.now())
            .isAllDay(true)
            .isPlace(false)
            .isOnline(false)
            .location("Lviv")
            .url("https://example.com/allday")
            .build();
        eventDateInfoRepo.save(eventDateInfo);

        List<EventDateInfo> foundEventDateInfos = eventDateInfoRepo.findByIsAllDayTrue();

        assertEquals(1, foundEventDateInfos.size());
        assertTrue(foundEventDateInfos.get(0).isAllDay());
    }
}
