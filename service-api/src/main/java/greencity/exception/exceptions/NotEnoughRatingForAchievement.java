package greencity.exception.exceptions;

/**
 * Exception that we get when we try to assign an Achievement to the User, whose
 * rating is not sufficient for this Achievement
 *
 * @author Mykhailo Derecha
 * @version 1.0
 */
public class NotEnoughRatingForAchievement extends RuntimeException {

    /**
     * Constructor for NotEnoughRatingForAchievement.
     *
     * @param msg - exc message.
     */
    public NotEnoughRatingForAchievement(String msg) {
        super(msg);
    }

}
