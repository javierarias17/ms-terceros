package co.edu.unbosque.tercero.domain.model.exception;

/**
 * Raised when a requested resource does not exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
