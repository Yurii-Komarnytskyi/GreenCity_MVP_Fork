package greencity.repository;

import greencity.entity.Event;
import greencity.entity.EventDateInfo;
import greencity.entity.User;
import greencity.enums.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
public class EventRepoTest {

    @Autowired
    private EventRepo eventRepo;

    @Autowired
    private EventDateInfoRepo eventDateInfoRepo;

    @Autowired
    private UserRepo userRepo;

    private User author;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        eventRepo.deleteAll();

        author = new User();
        author.setFirstName("John");
        author.setEmail("john.doe@mail.com");
        author.setDateOfRegistration(LocalDateTime.now());
        author.setName("John Doe");
        author.setRefreshTokenKey("token");
        author.setRole(Role.ROLE_USER);
        userRepo.save(author);
    }

    @AfterEach
    void tearDown() {
        eventDateInfoRepo.deleteAll();
        eventRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void testFindUserEventsByTime_Past() {

        LocalDateTime now = LocalDateTime.now();

        Event event = new Event();
        event.setTitle("Past Event");
        event.setDescription("Test event");
        event.setCreationDate(ZonedDateTime.now());
        event.setAuthor(author);
        event.setOpen(true);
        event.setDuration(120);

        eventRepo.save(event);

        EventDateInfo eventDateInfo = new EventDateInfo();
        eventDateInfo.setEvent(event);
        eventDateInfo.setEventDate(LocalDate.now());
        eventDateInfo.setEventTimeStart(now.minusDays(5));
        eventDateInfo.setEventTimeEnd(now.minusDays(5).plusHours(2));

        eventDateInfoRepo.save(eventDateInfo);

        Pageable pageable = PageRequest.of(0, 1);

        Page<Event> events = eventRepo.findUserEventsByTime(author.getId(), now, "PAST", pageable);

        assertEquals(1, events.getContent().size());
        assertEquals("Past Event", events.getContent().get(0).getTitle());
    }

    @Test
    void testFindUserEventsByTime_Live() {

        LocalDateTime now = LocalDateTime.now();

        Event event = new Event();
        event.setTitle("Live Event");
        event.setDescription("Test event");
        event.setCreationDate(ZonedDateTime.now());
        event.setAuthor(author);
        event.setOpen(true);
        event.setDuration(120);

        eventRepo.save(event);

        EventDateInfo eventDateInfo = new EventDateInfo();
        eventDateInfo.setEvent(event);
        eventDateInfo.setEventDate(LocalDate.now());
        eventDateInfo.setEventTimeStart(now.minusMinutes(1));
        eventDateInfo.setEventTimeEnd(now.plusHours(2));

        eventDateInfoRepo.save(eventDateInfo);

        Pageable pageable = PageRequest.of(0, 1);

        Page<Event> events = eventRepo.findUserEventsByTime(author.getId(), now, "LIVE", pageable);

        assertEquals(1, events.getContent().size());
        assertEquals("Live Event", events.getContent().get(0).getTitle());
    }

    @Test
    void testFindUserEventsByTime_Upcoming() {
        LocalDateTime now = LocalDateTime.now();

        Event event = new Event();
        event.setTitle("Upcoming Event");
        event.setDescription("Test event");
        event.setCreationDate(ZonedDateTime.now());
        event.setAuthor(author);
        event.setOpen(true);
        event.setDuration(120);

        eventRepo.save(event);

        EventDateInfo eventDateInfo = new EventDateInfo();
        eventDateInfo.setEvent(event);
        eventDateInfo.setEventDate(LocalDate.now());
        eventDateInfo.setEventTimeStart(now.plusDays(2));
        eventDateInfo.setEventTimeEnd(now.plusDays(2).plusHours(2));

        eventDateInfoRepo.save(eventDateInfo);

        Pageable pageable = PageRequest.of(0, 1);

        Page<Event> events = eventRepo.findUserEventsByTime(author.getId(), now, "UPCOMING", pageable);

        assertEquals(1, events.getContent().size());
        assertEquals("Upcoming Event", events.getContent().get(0).getTitle());
    }

    @Test
    void testFindAllByAuthorId() {
        Event event1 = new Event();
        event1.setTitle("Online Event");
        event1.setDescription("This is an online event");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(120);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Offline Event");
        event2.setDescription("This is an offline event");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(true);
        event2.setDuration(90);
        eventRepo.save(event2);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setEventDate(LocalDate.now());
        eventDateInfo1.setEventTimeStart(LocalDateTime.now().minusHours(1));
        eventDateInfo1.setEventTimeEnd(LocalDateTime.now().plusHours(1));
        eventDateInfo1.setOnline(true);
        eventDateInfoRepo.save(eventDateInfo1);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setEventDate(LocalDate.now());
        eventDateInfo2.setEventTimeStart(LocalDateTime.now().minusHours(1));
        eventDateInfo2.setEventTimeEnd(LocalDateTime.now().plusHours(1));
        eventDateInfo2.setOnline(false);
        eventDateInfoRepo.save(eventDateInfo2);

        List<Event> events = eventRepo.findAllByAuthorId(author.getId());

        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(event -> event.getTitle().equals("Online Event")));
        assertTrue(events.stream().anyMatch(event -> event.getTitle().equals("Offline Event")));
    }

    @Test
    void testFindEventsByAuthorAndFirstDayOnlineStatus() {

        Event event1 = new Event();
        event1.setTitle("Online Event");
        event1.setDescription("This is an online event");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Offline Event");
        event2.setDescription("This is an offline event");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        eventRepo.save(event2);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setEventDate(LocalDate.now());
        eventDateInfo1.setEventTimeStart(LocalDateTime.now().minusHours(1));
        eventDateInfo1.setEventTimeEnd(LocalDateTime.now().plusHours(1));
        eventDateInfo1.setOnline(true);
        eventDateInfoRepo.save(eventDateInfo1);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setEventDate(LocalDate.now());
        eventDateInfo2.setEventTimeStart(LocalDateTime.now().minusHours(1));
        eventDateInfo2.setEventTimeEnd(LocalDateTime.now().plusHours(1));
        eventDateInfo2.setOnline(false);
        eventDateInfo2.setLocation("Some Location");
        eventDateInfoRepo.save(eventDateInfo2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> onlineEvents = eventRepo.findEventsByAuthorAndFirstDayOnlineStatus(author.getId(), true, pageable);
        assertEquals(1, onlineEvents.getContent().size());
        assertEquals("Online Event", onlineEvents.getContent().get(0).getTitle());

        Page<Event> offlineEvents =
            eventRepo.findEventsByAuthorAndFirstDayOnlineStatus(author.getId(), false, pageable);
        assertEquals(1, offlineEvents.getContent().size());
        assertEquals("Offline Event", offlineEvents.getContent().get(0).getTitle());
    }

    @Test
    void testFindById() {
        Event event = new Event();
        event.setTitle("Sample Event");
        event.setDescription("Event description");
        event.setCreationDate(ZonedDateTime.now());
        event.setAuthor(author);
        event.setOpen(true);
        event.setDuration(60);
        eventRepo.save(event);

        Optional<Event> foundEvent = eventRepo.findById(event.getId());

        assertTrue(foundEvent.isPresent());
        assertEquals(event.getId(), foundEvent.get().getId());
        assertEquals(event.getTitle(), foundEvent.get().getTitle());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Event> foundEvent = eventRepo.findById(999L);
        assertTrue(foundEvent.isEmpty());
    }

    @Test
    void testFindByTitleIgnoreCase() {
        Event event1 = new Event();
        event1.setTitle("Spring Boot Event");
        event1.setDescription("Description 1");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(120);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("spring boot event");
        event2.setDescription("Description 2");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(false);
        event2.setDuration(90);
        eventRepo.save(event2);

        List<Event> foundEvents = eventRepo.findByTitleIgnoreCase("spring boot event");

        assertEquals(2, foundEvents.size());
        assertTrue(foundEvents.stream().anyMatch(event -> event.getTitle().equals("Spring Boot Event")));
        assertTrue(foundEvents.stream().anyMatch(event -> event.getTitle().equals("spring boot event")));
    }

    @Test
    void testCountAllOpenEvents() {
        Event event1 = new Event();
        event1.setTitle("Open Event 1");
        event1.setDescription("Open Event Description");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(100);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Closed Event");
        event2.setDescription("Closed Event Description");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(false);
        event2.setDuration(80);
        eventRepo.save(event2);

        Long openEventCount = eventRepo.countAllOpenEvents();

        assertEquals(1, openEventCount);
    }

    @Test
    void testFindAllByCreationDateBetween() {
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now().plusDays(1);

        Event event1 = new Event();
        event1.setTitle("Event within range");
        event1.setDescription("Description");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(90);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Event outside range");
        event2.setDescription("Description");
        event2.setCreationDate(ZonedDateTime.now().minusDays(2));
        event2.setAuthor(author);
        event2.setOpen(false);
        event2.setDuration(60);
        eventRepo.save(event2);

        List<Event> foundEvents = eventRepo.findAllByCreationDateBetween(startDate, endDate);

        assertEquals(1, foundEvents.size());
        assertEquals("Event within range", foundEvents.get(0).getTitle());
    }

    @Test
    void findAllByAuthorOrParticipantTest() {
        Event event1 = new Event();
        event1.setTitle("First test event");
        event1.setDescription("Description");
        event1.setCreationDate(ZonedDateTime.of(2024, 12, 3, 12, 30, 0, 0, ZoneId.of("Europe/Kyiv")));
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(1);
        eventRepo.save(event1);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setEventDate(LocalDate.of(2024, 1, 1));
        eventDateInfo1.setEventTimeStart(LocalDateTime.now().minusHours(1));
        eventDateInfo1.setEventTimeEnd(LocalDateTime.now().plusHours(1));
        eventDateInfo1.setOnline(true);
        eventDateInfoRepo.save(eventDateInfo1);

        Event event2 = new Event();
        event2.setTitle("Second event");
        event2.setDescription("Description2");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(true);
        event2.setDuration(1);
        eventRepo.save(event2);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setEventDate(LocalDate.now());
        eventDateInfo2.setEventTimeStart(LocalDateTime.now().minusHours(1));
        eventDateInfo2.setEventTimeEnd(LocalDateTime.now().plusHours(1));
        eventDateInfo2.setOnline(true);
        eventDateInfoRepo.save(eventDateInfo2);

        Pageable pageable = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "creationDate"));

        Page<Event> events = eventRepo.findAllByAuthorOrParticipant(author.getId(), pageable);

        assertEquals(1, events.getContent().size());
        assertEquals(2, events.getTotalElements());
        assertEquals("Second event", events.getContent().get(0).getTitle());
    }

    @Test
    void testFindByTitleContainingIgnoreCaseSortedByTitle() {

        Event event1 = new Event();
        event1.setTitle("Spring Boot Basics");
        event1.setDescription("An event about Spring Boot");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(120);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Spring Boot Advanced");
        event2.setDescription("An advanced event about Spring Boot");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(true);
        event2.setDuration(180);
        eventRepo.save(event2);

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title")));

        Page<Event> result = eventRepo.findByTitleContainingIgnoreCaseSortedByTitle("Spring Boot", pageable);

        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().get(0).getTitle().compareTo(result.getContent().get(1).getTitle()) < 0);
    }

    @Test
    void testFindByTitleContainingIgnoreCaseSortedByDate() {
        LocalDateTime now = LocalDateTime.now();

        Event event1 = new Event();
        event1.setTitle("Past Event");
        event1.setDescription("Test event");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(2);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Upcoming Event");
        event2.setDescription("Test event");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(true);
        event2.setDuration(2);
        eventRepo.save(event2);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setEventDate(LocalDate.now());
        eventDateInfo1.setEventTimeStart(now.minusDays(5));
        eventDateInfo1.setEventTimeEnd(now.minusDays(5).plusHours(2));
        eventDateInfoRepo.save(eventDateInfo1);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setEventDate(LocalDate.now());
        eventDateInfo2.setEventTimeStart(now.plusDays(5));
        eventDateInfo2.setEventTimeEnd(now.plusDays(5).plusHours(2));
        eventDateInfoRepo.save(eventDateInfo2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> events = eventRepo.findByTitleContainingIgnoreCaseSortedByDate("", pageable);

        assertEquals(2, events.getContent().size());
        assertEquals("Past Event", events.getContent().get(0).getTitle());
        assertEquals("Upcoming Event", events.getContent().get(1).getTitle());
    }

    @Test
    void findAllSortedByStartDateAscTest() {
        LocalDateTime now = LocalDateTime.now();

        Event event1 = new Event();
        event1.setTitle("Event one");
        event1.setDescription("Test event one");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(120);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Event two");
        event2.setDescription("Test event two");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(true);
        event2.setDuration(120);
        eventRepo.save(event2);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setNumOfDayInEvent(1);
        eventDateInfo1.setEventDate(LocalDate.now());
        eventDateInfo1.setEventTimeStart(now.minusDays(5));
        eventDateInfo1.setEventTimeEnd(now.minusDays(5).plusHours(2));
        eventDateInfoRepo.save(eventDateInfo1);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setNumOfDayInEvent(1);
        eventDateInfo2.setEventDate(LocalDate.now());
        eventDateInfo2.setEventTimeStart(now.plusDays(5));
        eventDateInfo2.setEventTimeEnd(now.plusDays(5).plusHours(2));
        eventDateInfoRepo.save(eventDateInfo2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Event> events = eventRepo.findAllSortedByStartDateAsc(pageable);

        assertEquals(2, events.getContent().size());
        assertEquals(2, events.getTotalElements());
        assertEquals("Event one", events.getContent().get(0).getTitle());
    }

    @Test
    void findAllSortedByStartDateAscNoPageSizeTest() {
        LocalDateTime now = LocalDateTime.now();

        Event event1 = new Event();
        event1.setTitle("Event one");
        event1.setDescription("Test event one");
        event1.setCreationDate(ZonedDateTime.now());
        event1.setAuthor(author);
        event1.setOpen(true);
        event1.setDuration(120);
        eventRepo.save(event1);

        Event event2 = new Event();
        event2.setTitle("Event two");
        event2.setDescription("Test event two");
        event2.setCreationDate(ZonedDateTime.now());
        event2.setAuthor(author);
        event2.setOpen(true);
        event2.setDuration(120);
        eventRepo.save(event2);

        EventDateInfo eventDateInfo1 = new EventDateInfo();
        eventDateInfo1.setEvent(event1);
        eventDateInfo1.setNumOfDayInEvent(1);
        eventDateInfo1.setEventDate(LocalDate.now());
        eventDateInfo1.setEventTimeStart(now.minusDays(5));
        eventDateInfo1.setEventTimeEnd(now.minusDays(5).plusHours(2));
        eventDateInfoRepo.save(eventDateInfo1);

        EventDateInfo eventDateInfo2 = new EventDateInfo();
        eventDateInfo2.setEvent(event2);
        eventDateInfo2.setNumOfDayInEvent(1);
        eventDateInfo2.setEventDate(LocalDate.now());
        eventDateInfo2.setEventTimeStart(now.plusDays(5));
        eventDateInfo2.setEventTimeEnd(now.plusDays(5).plusHours(2));
        eventDateInfoRepo.save(eventDateInfo2);

        Pageable pageable = Pageable.unpaged();

        Page<Event> events = eventRepo.findAllSortedByStartDateAsc(pageable);

        assertEquals(2, events.getContent().size());
        assertEquals(2, events.getTotalElements());
        assertEquals("Event one", events.getContent().get(0).getTitle());
    }
}
