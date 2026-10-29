package co.edu.unbosque.tercero.domain.ports;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import reactor.core.publisher.Mono;

/**
 * Outbound port for sending transactional emails (e.g. via SendGrid). Implementations should
 * fail the {@link Mono} when the email provider rejects the send, so the use case can translate
 * that into a proper error response instead of silently losing the notification.
 */
public interface IEmailNotificationGateway {

    Mono<Void> sendRegistrationConfirmation(RegistrationNotificationRequest request);
}
