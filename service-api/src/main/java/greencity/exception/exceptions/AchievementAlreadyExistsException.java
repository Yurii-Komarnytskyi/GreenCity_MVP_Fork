package greencity.exception.exceptions;

/**
 * Exception that we get when we try to assign an Achievement to the User, that
 * he already has
 *
 * @author Mykhailo Derecha
 * @version 1.0
 */
public class AchievementAlreadyExistsException extends RuntimeException {

    /**
     * Constructor for AchievementAlreadyExistsException.
     *
     * @param msg - exc message.
     */
    public AchievementAlreadyExistsException(String msg) {
        super(msg);
    }
}
