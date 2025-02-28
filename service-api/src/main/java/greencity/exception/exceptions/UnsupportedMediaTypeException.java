package greencity.exception.exceptions;

/**
 * Exception thrown when an unsupported media type is provided.
 *
 * @author Viktoriia Rychenko
 */
public class UnsupportedMediaTypeException extends RuntimeException {
    /**
     * Constructor for UnsupportedMediaTypeException.
     *
     * @param message - providing message.
     */
    public UnsupportedMediaTypeException(String message) {
        super(message);
    }
}