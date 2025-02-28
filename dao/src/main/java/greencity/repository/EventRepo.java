package greencity.repository;

import greencity.entity.Event;
import greencity.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepo extends JpaRepository<Event, Long> {

    /**
     * Method to get an event by its ID.
     *
     * @param id ID of the event.
     * @return an {@link Optional} of {@link Event}.
     */
    Optional<Event> findById(Long id);

    /**
     * Method to get all events with a specific title (case-insensitive).
     *
     * @param title the title to search for.
     * @return list of {@link Event} instances with the specified title.
     */
    List<Event> findByTitleIgnoreCase(String title);

    /**
     * Method to get the count of all open events.
     *
     * @return count of open events.
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.isOpen = true")
    Long countAllOpenEvents();

    /**
     * Method to find all events within a specific date range.
     *
     * @param startDate start of the range.
     * @param endDate   end of the range.
     * @return list of {@link Event} instances within the date range.
     */
    List<Event> findAllByCreationDateBetween(ZonedDateTime startDate, ZonedDateTime endDate);

    @Query("""
        SELECT e FROM Event e
        WHERE (e.author.id = :userId OR EXISTS (
            SELECT p FROM Participation p WHERE p.id.event.id = e.id AND p.id.user.id = :userId
        ))
        AND (
            (:type = 'PAST' AND EXISTS (
                SELECT d FROM EventDateInfo d WHERE d.event = e AND d.eventTimeEnd < :now
            ))
            OR (:type = 'LIVE' AND EXISTS (
                SELECT d FROM EventDateInfo d WHERE d.event = e AND d.eventTimeStart <= :now AND d.eventTimeEnd >= :now
            ))
            OR (:type = 'UPCOMING' AND EXISTS (
                SELECT d FROM EventDateInfo d WHERE d.event = e AND d.eventTimeStart > :now
            ))
        )
        """)
    Page<Event> findUserEventsByTime(@Param("userId") Long userId,
        @Param("now") LocalDateTime now,
        @Param("type") String type,
        Pageable pageable);

    List<Event> findAllByAuthorId(Long userId);

    @Query("""
        SELECT e FROM Event e
        WHERE e.author.id = :userId
        OR EXISTS (
            SELECT p FROM Participation p WHERE p.id.event.id = e.id AND p.id.user.id = :userId
        )
        """)
    Page<Event> findAllByAuthorOrParticipant(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN EventDateInfo edi ON edi.event.id = e.id " +
        "WHERE e.author.id = :authorId " +
        "AND edi.isOnline = :isOnline " +
        "AND (" +
        "   (:isOnline = true AND edi.eventDate = (SELECT MIN(edi2.eventDate) FROM EventDateInfo edi2 WHERE edi2.event.id = e.id)) "
        +
        "   OR " +
        "   (:isOnline = false AND edi.location = (SELECT edi3.location FROM EventDateInfo edi3 WHERE edi3.event.id = e.id))"
        +
        ")")
    Page<Event> findEventsByAuthorAndFirstDayOnlineStatus(@Param("authorId") Long authorId,
        @Param("isOnline") boolean isOnline,
        Pageable pageable);

    @Query("""
            SELECT e FROM Event e
            JOIN EventDateInfo edi ON edi.event = e
            WHERE edi.numOfDayInEvent = 1
            ORDER BY edi.eventTimeStart ASC
        """)
    Page<Event> findAllSortedByStartDateAsc(Pageable pageable);

    @Query("SELECT e FROM Event e " +
        "WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
        "ORDER BY e.title ASC")
    Page<Event> findByTitleContainingIgnoreCaseSortedByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT e FROM Event e " +
        "JOIN EventDateInfo edi ON edi.event = e " +
        "WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
        "ORDER BY edi.eventTimeStart ASC")
    Page<Event> findByTitleContainingIgnoreCaseSortedByDate(@Param("title") String title, Pageable pageable);
}
