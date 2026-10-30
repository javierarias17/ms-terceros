package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroDocument;
import co.edu.unbosque.tercero.domain.model.TerceroDocumentFile;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.TerceroRegistrationRequest;
import co.edu.unbosque.tercero.domain.ports.IDocumentStorageGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Finalizes a tercero registration: uploads every submitted document to storage, persists the
 * registration with their URLs, and sends the confirmation email (reusing
 * {@link SendRegistrationConfirmationUseCase}). A failure sending the email does not undo the
 * registration — it only gets logged, since the registration itself already succeeded.
 */
@Slf4j
@RequiredArgsConstructor
public class FinalizeTerceroRegistrationUseCase {

    private final IDocumentStorageGateway documentStorageGateway;
    private final ITerceroRegistrationRepository terceroRegistrationRepository;
    private final SendRegistrationConfirmationUseCase sendRegistrationConfirmationUseCase;

    public Mono<TerceroRegistration> execute(TerceroRegistrationRequest request) {
        log.info("Finalizando registro de tercero '{}' ({} documento(s) recibidos)",
                request.getNombre(), request.getDocuments().size());

        return uploadDocuments(request)
                .flatMap(documents -> terceroRegistrationRepository.save(buildRegistration(request, documents)))
                .doOnError(ex -> log.error("Fallo al finalizar el registro de '{}': {}", request.getNombre(), ex.getMessage(), ex))
                .flatMap(this::sendConfirmationThenReturn);
    }

    private Mono<List<TerceroDocument>> uploadDocuments(TerceroRegistrationRequest request) {
        return Flux.fromIterable(request.getDocuments())
                .flatMap(this::uploadOne)
                .collectList();
    }

    private Mono<TerceroDocument> uploadOne(TerceroDocumentFile document) {
        return documentStorageGateway.upload(document.getContent(), document.getFileName(), document.getContentType())
                .map(url -> TerceroDocument.builder()
                        .documentKey(document.getKey())
                        .url(url)
                        .fileName(document.getFileName())
                        .contentType(document.getContentType())
                        .uploadedAt(Instant.now())
                        .build());
    }

    private TerceroRegistration buildRegistration(TerceroRegistrationRequest request, List<TerceroDocument> documents) {
        return TerceroRegistration.builder()
                .calidad(request.getCalidad())
                .nacionalidad(request.getNacionalidad())
                .tipoPersona(request.getTipoPersona())
                .correo(request.getCorreo())
                .nombre(request.getNombre())
                .cedula(request.getCedula())
                .fechaExpedicionCedula(request.getFechaExpedicionCedula())
                .lugarNacimiento(request.getLugarNacimiento())
                .paisNacimiento(request.getPaisNacimiento())
                .nit(request.getNit())
                .celular(request.getCelular())
                .direccion(request.getDireccion())
                .documents(documents)
                .estado(RegistrationStatus.REGISTRADO)
                .fechaRegistro(Instant.now())
                .build();
    }

    private Mono<TerceroRegistration> sendConfirmationThenReturn(TerceroRegistration saved) {
        RegistrationNotificationRequest notification = RegistrationNotificationRequest.builder()
                .recipientEmail(saved.getCorreo())
                .nombreCompleto(saved.getNombre())
                .tipoTercero(saved.getCalidad())
                .numeroIdentificacion(saved.getCedula())
                .build();

        return sendRegistrationConfirmationUseCase.execute(notification)
                .onErrorResume(ex -> {
                    log.error("Registro {} se guardó, pero el correo de confirmación falló: {}", saved.getId(), ex.getMessage());
                    return Mono.empty();
                })
                .thenReturn(saved);
    }
}
