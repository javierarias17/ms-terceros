package co.edu.unbosque.tercero.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroDocumentFile;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.TerceroRegistrationRequest;
import co.edu.unbosque.tercero.domain.ports.IDocumentStorageGateway;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FinalizeTerceroRegistrationUseCaseTest {

    private final IDocumentStorageGateway documentStorageGateway = mock(IDocumentStorageGateway.class);
    private final ITerceroRegistrationRepository terceroRegistrationRepository = mock(ITerceroRegistrationRepository.class);
    private final IEmailNotificationGateway emailNotificationGateway = mock(IEmailNotificationGateway.class);
    private final SendRegistrationConfirmationUseCase sendRegistrationConfirmationUseCase =
            new SendRegistrationConfirmationUseCase(emailNotificationGateway);
    private final FinalizeTerceroRegistrationUseCase useCase = new FinalizeTerceroRegistrationUseCase(
            documentStorageGateway, terceroRegistrationRepository, sendRegistrationConfirmationUseCase);

    private TerceroRegistrationRequest.TerceroRegistrationRequestBuilder baseRequest() {
        return TerceroRegistrationRequest.builder()
                .calidad("Cliente")
                .correo("ana.gomez@unbosque.edu.co")
                .nombre("Ana Gomez")
                .cedula("123456789")
                .documents(List.of(
                        TerceroDocumentFile.builder().key("cli_cedula").fileName("cedula.pdf")
                                .contentType("application/pdf").content(new byte[]{1, 2, 3}).build(),
                        TerceroDocumentFile.builder().key("cli_rut").fileName("rut.pdf")
                                .contentType("application/pdf").content(new byte[]{4, 5, 6}).build()));
    }

    @Test
    void shouldUploadDocumentsSaveRegistrationAndSendConfirmation() {
        when(documentStorageGateway.upload(any(), eq("cedula.pdf"), any())).thenReturn(Mono.just("https://blob/cedula.pdf"));
        when(documentStorageGateway.upload(any(), eq("rut.pdf"), any())).thenReturn(Mono.just("https://blob/rut.pdf"));
        when(terceroRegistrationRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(emailNotificationGateway.sendRegistrationConfirmation(any())).thenReturn(Mono.empty());

        TerceroRegistration result = useCase.execute(baseRequest().build()).block();

        assertThat(result).isNotNull();
        assertThat(result.getEstado()).isEqualTo(RegistrationStatus.REGISTRADO);
        assertThat(result.getDocuments()).hasSize(2);
        assertThat(result.getDocuments())
                .extracting("documentKey", "url")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("cli_cedula", "https://blob/cedula.pdf"),
                        org.assertj.core.groups.Tuple.tuple("cli_rut", "https://blob/rut.pdf"));
    }

    @Test
    void shouldKeepRegistrationEvenWhenConfirmationEmailFails() {
        when(documentStorageGateway.upload(any(), any(), any())).thenReturn(Mono.just("https://blob/file.pdf"));
        when(terceroRegistrationRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(emailNotificationGateway.sendRegistrationConfirmation(any()))
                .thenReturn(Mono.error(new RuntimeException("SendGrid caído")));

        StepVerifier.create(useCase.execute(baseRequest().build()))
                .assertNext(result -> assertThat(result.getEstado()).isEqualTo(RegistrationStatus.REGISTRADO))
                .verifyComplete();
    }

    private static <T> T eq(T value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
