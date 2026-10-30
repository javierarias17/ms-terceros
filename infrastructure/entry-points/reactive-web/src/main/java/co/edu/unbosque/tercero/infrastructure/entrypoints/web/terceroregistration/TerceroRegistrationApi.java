package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Contrato público (OpenAPI) del endpoint que finaliza un registro de tercero, separado de
 * {@link TerceroRegistrationController} por la misma razón que {@code DocumentValidationApi}.
 */
@Tag(name = "Registro de terceros", description = "Finaliza el registro de un tercero: sube sus documentos a Azure Blob Storage, guarda el registro completo en PostgreSQL, y envía el correo de confirmación.")
@RequestMapping("/v1/tercero-registrations")
public interface TerceroRegistrationApi {

    @Operation(
            summary = "Finalizar el registro de un tercero",
            description = """
                    Se llama una sola vez, cuando el solicitante confirma "Finalizar registro" en el frontend.

                    El cuerpo es `multipart/form-data` con dos tipos de parte:

                    - **Campos de texto**, con estos nombres exactos: `calidad`, `nacionalidad`, `tipoPersona`, `correo`, `nombre`, `cedula`, `fechaExpedicionCedula`, `lugarNacimiento`, `paisNacimiento`, `nit`, `celular`, `direccion`.
                    - **Archivos**: una parte de tipo archivo por cada documento subido. El *nombre* de cada parte es la clave interna que usa el frontend para ese documento (ej. `prov_cedula`, `cli_rut`, `prov_certBancaria`) — la cantidad y los nombres varían según `calidad`/`tipoPersona`/`nacionalidad`, no son fijos.

                    Sube cada archivo, guarda el registro con estado `REGISTRADO`, y envía el correo de confirmación — si el correo falla, el registro igual queda guardado.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro guardado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = "{ \"id\": \"3f2e1a9c-7b4d-4e2a-9c1a-2d6f8e0b1a3c\", \"estado\": \"REGISTRADO\" }"))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Falta un campo obligatorio o el multipart está mal formado.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponseDto.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "No se pudo subir algún archivo o guardar el registro.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponseDto.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<TerceroRegistrationResponseDto> register(ServerWebExchange exchange);
}
