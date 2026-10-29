package co.edu.unbosque.tercero.domain.model;

import lombok.Builder;
import lombok.Getter;

/**
 * Input for {@link co.edu.unbosque.tercero.domain.usecases.SendRegistrationConfirmationUseCase}:
 * the data needed to send the applicant a confirmation email once they finish registering as a
 * tercero (after all their documents were validated on the frontend).
 */
@Getter
@Builder
public class RegistrationNotificationRequest {

    private final String recipientEmail;
    private final String nombreCompleto;
    private final String tipoTercero;
    private final String numeroIdentificacion;
}
