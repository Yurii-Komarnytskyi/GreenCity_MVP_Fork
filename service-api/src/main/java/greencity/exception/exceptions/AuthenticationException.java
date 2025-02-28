package greencity.exception.exceptions;

/**
 * Exception that represents an authentication failure. This exception is thrown
 * when the authentication process fails due to invalid credentials or other
 * reasons.
 *
 * @author Viktoriia Rychenko
 * @version 1.0
 */
public class AuthenticationException extends RuntimeException {
    /**
     * Constructor for AuthenticationException with a custom message.
     *
     * @param message the message describing the reason for the authentication
     *                failure.
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Constructor for AuthenticationException with a custom message and a cause.
     *
     * @param message the message describing the reason for the authentication
     *                failure.
     * @param cause   the root cause of the exception.
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
