package co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Cuerpo de error estándar devuelto por cualquier endpoint cuando algo falla.")
public class ApiErrorResponseDto {

    @Schema(description = "Código HTTP de la respuesta.", example = "422")
    private int statusCode;

    @Schema(description = "Nombre del código HTTP.", example = "UNPROCESSABLE_ENTITY")
    private String status;

    @Schema(description = "Mensaje legible explicando qué falló.", example = "No se pudo clasificar el documento")
    private String message;

    @Schema(description = "Momento en que ocurrió el error, en ISO-8601.", example = "2026-10-26T20:19:28.305Z")
    private String timestamp;

    @Schema(description = "Valor del header Transaction-Id de la petición, si se envió. null si no se envió ninguno.", example = "null")
    private String transactionId;
}
