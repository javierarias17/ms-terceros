package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.model.exception.BusinessException;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Sends the applicant a confirmation email once they finish registering as a tercero.
 */
@RequiredArgsConstructor
public class SendRegistrationConfirmationUseCase {

    private final IEmailNotificationGateway emailNotificationGateway;

    public Mono<Void> execute(RegistrationNotificationRequest request) {
        if (request.getRecipientEmail() == null || request.getRecipientEmail().isBlank()) {
            return Mono.error(new BusinessException("EMAIL_REQUIRED", "recipientEmail es obligatorio"));
        }
        return emailNotificationGateway.sendRegistrationConfirmation(request);
    }
}
