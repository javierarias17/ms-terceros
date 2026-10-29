package co.edu.unbosque.tercero.infrastructure.entrypoints.web.registrationconfirmation;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.usecases.SendRegistrationConfirmationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RegistrationConfirmationController implements RegistrationConfirmationApi {

    private final SendRegistrationConfirmationUseCase sendRegistrationConfirmationUseCase;

    @Override
    public Mono<RegistrationConfirmationResponseDto> sendConfirmation(RegistrationConfirmationRequestDto request) {
        RegistrationNotificationRequest notification = RegistrationNotificationRequest.builder()
                .recipientEmail(request.email())
                .nombreCompleto(request.nombreCompleto())
                .tipoTercero(request.tipoTercero())
                .numeroIdentificacion(request.numeroIdentificacion())
                .build();

        return sendRegistrationConfirmationUseCase.execute(notification)
                .thenReturn(new RegistrationConfirmationResponseDto(true));
    }
}
