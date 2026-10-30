package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroDocument;
import co.edu.unbosque.tercero.domain.model.TerceroDocumentFile;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.TerceroRegistrationRequest;
import co.edu.unbosque.tercero.domain.usecases.FinalizeTerceroRegistrationUseCase;
import co.edu.unbosque.tercero.domain.usecases.GetTerceroRegistrationUseCase;
import co.edu.unbosque.tercero.domain.usecases.ListTerceroRegistrationsUseCase;
import co.edu.unbosque.tercero.domain.usecases.UpdateTerceroRegistrationStatusUseCase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class TerceroRegistrationController implements TerceroRegistrationApi {

    private static final Set<String> SCALAR_FIELDS = Set.of(
            "calidad", "nacionalidad", "tipoPersona", "correo", "nombre", "cedula",
            "fechaExpedicionCedula", "lugarNacimiento", "paisNacimiento", "nit", "celular", "direccion");

    private final FinalizeTerceroRegistrationUseCase finalizeTerceroRegistrationUseCase;
    private final ListTerceroRegistrationsUseCase listTerceroRegistrationsUseCase;
    private final GetTerceroRegistrationUseCase getTerceroRegistrationUseCase;
    private final UpdateTerceroRegistrationStatusUseCase updateTerceroRegistrationStatusUseCase;

    @Override
    public Mono<TerceroRegistrationResponseDto> register(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(this::toRequest)
                .flatMap(finalizeTerceroRegistrationUseCase::execute)
                .map(registration -> new TerceroRegistrationResponseDto(registration.getId(), registration.getEstado().name()));
    }

    @Override
    public Flux<TerceroRegistrationSummaryDto> list(String estado) {
        return listTerceroRegistrationsUseCase.execute(parseEstado(estado)).map(this::toSummaryDto);
    }

    @Override
    public Mono<TerceroRegistrationDetailDto> getById(String id) {
        return getTerceroRegistrationUseCase.execute(id).map(this::toDetailDto);
    }

    @Override
    public Mono<TerceroRegistrationDetailDto> approve(String id) {
        return updateTerceroRegistrationStatusUseCase.execute(id, RegistrationStatus.APROBADO, null)
                .map(this::toDetailDto);
    }

    @Override
    public Mono<TerceroRegistrationDetailDto> reject(String id, RejectRequestDto request) {
        if (request == null || request.motivo() == null || request.motivo().isBlank()) {
            throw new IllegalArgumentException("motivo es obligatorio para rechazar una solicitud");
        }
        return updateTerceroRegistrationStatusUseCase.execute(id, RegistrationStatus.RECHAZADO, request.motivo())
                .map(this::toDetailDto);
    }

    private RegistrationStatus parseEstado(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return RegistrationStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("estado debe ser REGISTRADO, APROBADO o RECHAZADO");
        }
    }

    private TerceroRegistrationSummaryDto toSummaryDto(TerceroRegistration registration) {
        String numeroIdentificacion = "Juridica".equalsIgnoreCase(registration.getTipoPersona())
                && registration.getNit() != null && !registration.getNit().isBlank()
                ? registration.getNit()
                : registration.getCedula();

        return new TerceroRegistrationSummaryDto(
                registration.getId(),
                registration.getCalidad(),
                registration.getTipoPersona(),
                registration.getNombre(),
                numeroIdentificacion,
                registration.getEstado().name(),
                registration.getFechaRegistro().toString());
    }

    private TerceroRegistrationDetailDto toDetailDto(TerceroRegistration registration) {
        List<TerceroDocumentDto> documents = registration.getDocuments().stream()
                .map(this::toDocumentDto)
                .toList();

        return new TerceroRegistrationDetailDto(
                registration.getId(),
                registration.getCalidad(),
                registration.getNacionalidad(),
                registration.getTipoPersona(),
                registration.getCorreo(),
                registration.getNombre(),
                registration.getCedula(),
                registration.getFechaExpedicionCedula(),
                registration.getLugarNacimiento(),
                registration.getPaisNacimiento(),
                registration.getNit(),
                registration.getCelular(),
                registration.getDireccion(),
                registration.getEstado().name(),
                registration.getMotivoRechazo(),
                registration.getFechaRegistro().toString(),
                documents);
    }

    private TerceroDocumentDto toDocumentDto(TerceroDocument document) {
        return new TerceroDocumentDto(
                document.getDocumentKey(),
                document.getFileName(),
                document.getUrl(),
                document.getUploadedAt().toString());
    }

    private Mono<TerceroRegistrationRequest> toRequest(MultiValueMap<String, Part> parts) {
        Map<String, String> scalarValues = new HashMap<>();
        List<Mono<TerceroDocumentFile>> fileMonos = new ArrayList<>();

        parts.forEach((name, partList) -> partList.forEach(part -> {
            if (part instanceof FilePart filePart) {
                fileMonos.add(readFile(name, filePart));
            } else if (part instanceof FormFieldPart formFieldPart && SCALAR_FIELDS.contains(name)) {
                scalarValues.put(name, formFieldPart.value());
            }
        }));

        return Flux.merge(fileMonos)
                .collectList()
                .map(documents -> TerceroRegistrationRequest.builder()
                        .calidad(scalarValues.get("calidad"))
                        .nacionalidad(scalarValues.get("nacionalidad"))
                        .tipoPersona(scalarValues.get("tipoPersona"))
                        .correo(scalarValues.get("correo"))
                        .nombre(scalarValues.get("nombre"))
                        .cedula(scalarValues.get("cedula"))
                        .fechaExpedicionCedula(scalarValues.get("fechaExpedicionCedula"))
                        .lugarNacimiento(scalarValues.get("lugarNacimiento"))
                        .paisNacimiento(scalarValues.get("paisNacimiento"))
                        .nit(scalarValues.get("nit"))
                        .celular(scalarValues.get("celular"))
                        .direccion(scalarValues.get("direccion"))
                        .documents(documents)
                        .build());
    }

    private Mono<TerceroDocumentFile> readFile(String key, FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    String contentType = filePart.headers().getContentType() != null
                            ? filePart.headers().getContentType().toString()
                            : "application/octet-stream";
                    return TerceroDocumentFile.builder()
                            .key(key)
                            .fileName(filePart.filename())
                            .contentType(contentType)
                            .content(bytes)
                            .build();
                });
    }
}
