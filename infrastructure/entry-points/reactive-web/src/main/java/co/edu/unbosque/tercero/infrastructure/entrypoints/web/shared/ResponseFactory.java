package co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@UtilityClass
public class ResponseFactory {

    public static <T> ApiSuccessResponse<T> buildSuccessResponse(HttpStatus statusCode, T data, String transactionId) {
        return ApiSuccessResponse.<T>builder()
                .statusCode(statusCode.value())
                .status("SUCCESS")
                .data(data)
                .timestamp(Instant.now().toString())
                .transactionId(transactionId)
                .build();
    }

    public static ApiErrorResponseDto buildErrorResponse(HttpStatus statusCode, String message, String transactionId) {
        return ApiErrorResponseDto.builder()
                .statusCode(statusCode.value())
                .status("ERROR")
                .message(message)
                .timestamp(Instant.now().toString())
                .transactionId(transactionId)
                .build();
    }
}
