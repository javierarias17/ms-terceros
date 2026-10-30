package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import reactor.core.publisher.Mono;

/**
 * Outbound port for sending transactional emails (e.g. via SendGrid). Implementations should
 * fail the {@link Mono} when the email provider rejects the send, so the use case can translate
 * that into a proper error response instead of silently losing the notification.
 */
public interface IEmailNotificationGateway {

    Mono<Void> sendRegistrationConfirmation(RegistrationNotificationRequest request);

    /**
     * Notifies the applicant that their registration was approved or rejected. Reads
     * {@code registration.getEstado()} (and {@code motivoRechazo} when rejected) to pick the
     * right template.
     */
    Mono<Void> sendStatusUpdate(TerceroRegistration registration);
}
