package co.edu.unbosque.tercero.infrastructure.entrypoints.web.registrationconfirmation;

import co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared.ApiErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

/**
 * Contrato público (OpenAPI) del endpoint de confirmación de registro, separado de
 * {@link RegistrationConfirmationController} por la misma razón que {@code DocumentValidationApi}.
 */
@Tag(name = "Confirmación de registro", description = "Envía al tercero un correo de confirmación cuando termina de registrarse, una vez sus documentos ya fueron validados.")
@RequestMapping("/v1/registration-confirmations")
public interface RegistrationConfirmationApi {

    @Operation(
            summary = "Enviar el correo de confirmación de registro",
            description = "Se llama una sola vez, cuando el solicitante confirma \"Finalizar registro\" en el frontend, después de que todos sus documentos ya quedaron validados.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "El correo fue aceptado por SendGrid para envío.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{ \"sent\": true }"))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Falta el email del destinatario.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "SendGrid rechazó el envío (ej. remitente no verificado, API key inválida).",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<RegistrationConfirmationResponseDto> sendConfirmation(@RequestBody RegistrationConfirmationRequestDto request);
}
