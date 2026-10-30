package co.edu.unbosque.tercero.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class UpdateTerceroRegistrationStatusUseCaseTest {

    private final ITerceroRegistrationRepository terceroRegistrationRepository = mock(ITerceroRegistrationRepository.class);
    private final IEmailNotificationGateway emailNotificationGateway = mock(IEmailNotificationGateway.class);
    private final UpdateTerceroRegistrationStatusUseCase useCase =
            new UpdateTerceroRegistrationStatusUseCase(terceroRegistrationRepository, emailNotificationGateway);

    @Test
    void shouldApproveAndNotify() {
        TerceroRegistration updated = TerceroRegistration.builder()
                .id("1").estado(RegistrationStatus.APROBADO).build();
        when(terceroRegistrationRepository.updateEstado("1", RegistrationStatus.APROBADO, null))
                .thenReturn(Mono.just(updated));
        when(emailNotificationGateway.sendStatusUpdate(updated)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("1", RegistrationStatus.APROBADO, null))
                .assertNext(result -> assertThat(result.getEstado()).isEqualTo(RegistrationStatus.APROBADO))
                .verifyComplete();
    }

    @Test
    void shouldKeepStatusChangeEvenWhenNotificationEmailFails() {
        TerceroRegistration updated = TerceroRegistration.builder()
                .id("1").estado(RegistrationStatus.RECHAZADO).motivoRechazo("Documento ilegible").build();
        when(terceroRegistrationRepository.updateEstado(eq("1"), eq(RegistrationStatus.RECHAZADO), any()))
                .thenReturn(Mono.just(updated));
        when(emailNotificationGateway.sendStatusUpdate(updated))
                .thenReturn(Mono.error(new RuntimeException("SendGrid caído")));

        StepVerifier.create(useCase.execute("1", RegistrationStatus.RECHAZADO, "Documento ilegible"))
                .assertNext(result -> assertThat(result.getEstado()).isEqualTo(RegistrationStatus.RECHAZADO))
                .verifyComplete();
    }

    @Test
    void shouldFailWithResourceNotFoundWhenMissing() {
        when(terceroRegistrationRepository.updateEstado(any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing", RegistrationStatus.APROBADO, null))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
