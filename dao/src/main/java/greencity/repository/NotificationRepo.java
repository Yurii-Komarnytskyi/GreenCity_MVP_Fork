package greencity.repository;

import greencity.entity.Notification;
import greencity.entity.User;
import greencity.enums.NotificationSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Provides an interface to manage {@link Notification} entity.
 */
@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    /**
     * method, that finds all the {@link Notification} by receiver as List.
     *
     * @param receiver - who is a notification receiver.
     * @return {@link List<Notification>} by it's code.
     * @author Mykhailo Derecha
     */
    List<Notification> findByReceiver(User receiver);

    /**
     * method, that finds all the {@link Notification} by receiver as Pageable.
     *
     * @param receiver - who is a notifications receiver, we want to gets.
     * @param pageable .
     * @return {@link Page<Notification>}.
     * @author Mykhailo Derecha
     */
    Page<Notification> findByReceiver(User receiver, Pageable pageable);

    /**
     * method, that finds all the {@link Notification} by sender.
     *
     * @param sender   code of the language.
     * @param pageable
     * @return {@link List<Notification>} by sender.
     * @author Mykhailo Derecha
     */
    Page<Notification> findBySender(User sender, Pageable pageable);

    /**
     * method, that finds all the {@link Notification} by section.
     *
     * @param section  code of the language.
     * @param pageable
     * @return {@link Page<Notification>} by section.
     * @author Mykhailo Derecha
     */
    Page<Notification> findBySection(NotificationSection section, Pageable pageable);

}
