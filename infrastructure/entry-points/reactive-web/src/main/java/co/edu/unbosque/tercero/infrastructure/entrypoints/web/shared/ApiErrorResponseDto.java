package co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Standard error body returned by {@link
 * co.edu.unbosque.tercero.infrastructure.entrypoints.web.middleware.exceptionhandling.GlobalExceptionHandler}.
 */
@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponseDto {

    private int statusCode;
    private String status;
    private String message;
    private String timestamp;
    private String transactionId;
}
