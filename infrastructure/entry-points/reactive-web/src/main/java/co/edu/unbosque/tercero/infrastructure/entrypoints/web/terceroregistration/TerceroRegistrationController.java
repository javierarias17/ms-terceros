package co.edu.unbosque.tercero.infrastructure.entrypoints.web.terceroregistration;

import co.edu.unbosque.tercero.domain.model.TerceroDocumentFile;
import co.edu.unbosque.tercero.domain.model.TerceroRegistrationRequest;
import co.edu.unbosque.tercero.domain.usecases.FinalizeTerceroRegistrationUseCase;
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

    @Override
    public Mono<TerceroRegistrationResponseDto> register(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(this::toRequest)
                .flatMap(finalizeTerceroRegistrationUseCase::execute)
                .map(registration -> new TerceroRegistrationResponseDto(registration.getId(), registration.getEstado().name()));
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
