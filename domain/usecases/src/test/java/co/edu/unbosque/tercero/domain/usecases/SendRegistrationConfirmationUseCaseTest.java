package co.edu.unbosque.tercero.domain.usecases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.RegistrationNotificationRequest;
import co.edu.unbosque.tercero.domain.model.exception.BusinessException;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SendRegistrationConfirmationUseCaseTest {

    private final IEmailNotificationGateway emailNotificationGateway = mock(IEmailNotificationGateway.class);
    private final SendRegistrationConfirmationUseCase useCase =
            new SendRegistrationConfirmationUseCase(emailNotificationGateway);

    @Test
    void shouldSendConfirmationWhenRecipientEmailIsPresent() {
        RegistrationNotificationRequest request = RegistrationNotificationRequest.builder()
                .recipientEmail("ana.gomez@unbosque.edu.co")
                .nombreCompleto("Ana Gomez")
                .tipoTercero("Proveedor")
                .numeroIdentificacion("123456789")
                .build();
        when(emailNotificationGateway.sendRegistrationConfirmation(request)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(request)).verifyComplete();

        verify(emailNotificationGateway).sendRegistrationConfirmation(request);
    }

    @Test
    void shouldFailWithoutCallingGatewayWhenRecipientEmailIsMissing() {
        RegistrationNotificationRequest request = RegistrationNotificationRequest.builder()
                .recipientEmail("  ")
                .nombreCompleto("Ana Gomez")
                .build();

        StepVerifier.create(useCase.execute(request))
                .expectError(BusinessException.class)
                .verify();

        verify(emailNotificationGateway, org.mockito.Mockito.never()).sendRegistrationConfirmation(any());
    }
}
