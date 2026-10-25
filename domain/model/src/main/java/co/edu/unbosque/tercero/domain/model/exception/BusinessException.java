package co.edu.unbosque.tercero.domain.model.exception;

import lombok.Getter;

/**
 * Raised when a business rule is violated. Entry points translate it into an HTTP response.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }
}
