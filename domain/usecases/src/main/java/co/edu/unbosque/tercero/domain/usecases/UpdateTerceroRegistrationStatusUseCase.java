package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Approves or rejects a tercero registration and notifies the applicant by email. A failure
 * sending that email does not undo the status change — same reasoning as
 * {@link FinalizeTerceroRegistrationUseCase}: the decision already happened, it just gets logged.
 */
@Slf4j
@RequiredArgsConstructor
public class UpdateTerceroRegistrationStatusUseCase {

    private final ITerceroRegistrationRepository terceroRegistrationRepository;
    private final IEmailNotificationGateway emailNotificationGateway;

    public Mono<TerceroRegistration> execute(String id, RegistrationStatus estado, String motivoRechazo) {
        return terceroRegistrationRepository.updateEstado(id, estado, motivoRechazo)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "No existe un registro de tercero con id " + id)))
                .flatMap(this::notifyThenReturn);
    }

    private Mono<TerceroRegistration> notifyThenReturn(TerceroRegistration updated) {
        return emailNotificationGateway.sendStatusUpdate(updated)
                .onErrorResume(ex -> {
                    log.error("Registro {} quedó en estado {}, pero el correo de notificación falló: {}",
                            updated.getId(), updated.getEstado(), ex.getMessage());
                    return Mono.empty();
                })
                .thenReturn(updated);
    }
}
