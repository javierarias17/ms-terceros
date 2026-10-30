package co.edu.unbosque.tercero.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroDocument;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.domain.ports.IDocumentStorageGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetTerceroRegistrationUseCaseTest {

    private final ITerceroRegistrationRepository terceroRegistrationRepository = mock(ITerceroRegistrationRepository.class);
    private final IDocumentStorageGateway documentStorageGateway = mock(IDocumentStorageGateway.class);
    private final GetTerceroRegistrationUseCase useCase =
            new GetTerceroRegistrationUseCase(terceroRegistrationRepository, documentStorageGateway);

    @Test
    void shouldReplaceDocumentUrlsWithSignedOnes() {
        TerceroDocument document = TerceroDocument.builder()
                .documentKey("cli_cedula").url("https://blob/raw-cedula.pdf").build();
        TerceroRegistration registration = TerceroRegistration.builder()
                .id("1").estado(RegistrationStatus.REGISTRADO).documents(List.of(document)).build();

        when(terceroRegistrationRepository.findById("1")).thenReturn(Mono.just(registration));
        when(documentStorageGateway.generateDownloadUrl("https://blob/raw-cedula.pdf"))
                .thenReturn(Mono.just("https://blob/raw-cedula.pdf?sas=token"));

        StepVerifier.create(useCase.execute("1"))
                .assertNext(result -> assertThat(result.getDocuments().get(0).getUrl())
                        .isEqualTo("https://blob/raw-cedula.pdf?sas=token"))
                .verifyComplete();
    }

    @Test
    void shouldFailWithResourceNotFoundWhenMissing() {
        when(terceroRegistrationRepository.findById(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
