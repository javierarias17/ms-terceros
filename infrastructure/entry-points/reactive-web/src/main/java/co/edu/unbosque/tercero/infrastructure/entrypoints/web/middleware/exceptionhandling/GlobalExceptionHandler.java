package co.edu.unbosque.tercero.infrastructure.entrypoints.web.middleware.exceptionhandling;

import co.edu.unbosque.tercero.domain.model.exception.BusinessException;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared.ApiErrorResponseDto;
import co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared.ResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * Translates domain/runtime exceptions into the standard {@link ApiErrorResponseDto} body.
 * Add one {@code @ExceptionHandler} per new exception type as the domain grows.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiErrorResponseDto>> handleResourceNotFound(
            ResourceNotFoundException ex,
            @RequestHeader(value = "Transaction-Id", required = false) String transactionId) {
        return buildResponse(HttpStatus.NOT_FOUND, ex, transactionId);
    }

    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ApiErrorResponseDto>> handleBusinessException(
            BusinessException ex,
            @RequestHeader(value = "Transaction-Id", required = false) String transactionId) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex, transactionId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ApiErrorResponseDto>> handleIllegalArgument(
            IllegalArgumentException ex,
            @RequestHeader(value = "Transaction-Id", required = false) String transactionId) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex, transactionId);
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiErrorResponseDto>> handleUnexpected(
            Exception ex,
            @RequestHeader(value = "Transaction-Id", required = false) String transactionId) {
        log.error("Unhandled error [transactionId={}]: {}", transactionId, ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex, transactionId);
    }

    private Mono<ResponseEntity<ApiErrorResponseDto>> buildResponse(
            HttpStatus status, Exception ex, String transactionId) {
        ApiErrorResponseDto body = ResponseFactory.buildErrorResponse(status, ex.getMessage(), transactionId);
        return Mono.just(ResponseEntity.status(status).body(body));
    }
}
