package greencity.exception.exceptions;

/**
 * Exception that we get when we try to assign an Achievement to the User, if
 * previous ones have not been opened yet
 *
 * @author Mykhailo Derecha
 * @version 1.0
 */
public class AchievementUnlockingException extends RuntimeException {

    /**
     * Constructor for AchievementUnlockingException.
     *
     * @param msg - exc message.
     */
    public AchievementUnlockingException(String msg) {
        super(msg);
    }
}
