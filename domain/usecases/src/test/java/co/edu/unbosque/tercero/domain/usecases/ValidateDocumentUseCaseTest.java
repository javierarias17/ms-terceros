package co.edu.unbosque.tercero.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.ApplicantType;
import co.edu.unbosque.tercero.domain.model.DocumentType;
import co.edu.unbosque.tercero.domain.model.DocumentValidationRequest;
import co.edu.unbosque.tercero.domain.model.DocumentValidationResult;
import co.edu.unbosque.tercero.domain.ports.IDocumentClassifierGateway;
import co.edu.unbosque.tercero.domain.ports.IDocumentExtractorGateway;
import java.util.Map;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ValidateDocumentUseCaseTest {

    private final IDocumentClassifierGateway classifierGateway = mock(IDocumentClassifierGateway.class);
    private final IDocumentExtractorGateway extractorGateway = mock(IDocumentExtractorGateway.class);
    private final ValidateDocumentUseCase useCase = new ValidateDocumentUseCase(
            classifierGateway, extractorGateway, "Persona Natural", "Persona Jurídica");

    @Test
    void shouldFailWhenDetectedTypeDoesNotMatchExpected() {
        byte[] content = "pdf".getBytes();
        when(classifierGateway.classify(content)).thenReturn(Mono.just(DocumentType.RUT));

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .expectedType(DocumentType.CEDULA)
                .documentContent(content)
                .numeroDocumento("123")
                .nombreCompleto("Ana Gomez")
                .build();

        StepVerifier.create(useCase.execute(request))
                .assertNext(result -> {
                    assertThat(result.isDocumentTypeMatch()).isFalse();
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.getFieldComparisons()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void shouldMatchCedulaFieldsIgnoringCaseAccentsAndOrder() {
        byte[] content = "pdf".getBytes();
        when(classifierGateway.classify(content)).thenReturn(Mono.just(DocumentType.CEDULA));
        when(extractorGateway.extractFields(DocumentType.CEDULA, content)).thenReturn(Mono.just(Map.of(
                "cedula_numero", "123456789",
                "cedula_nombres", "ANA MARIA",
                "cedula_apellidos", "GÓMEZ PÉREZ"
        )));

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .expectedType(DocumentType.CEDULA)
                .documentContent(content)
                .numeroDocumento("123456789")
                .nombreCompleto("gomez perez ana maria")
                .build();

        DocumentValidationResult result = useCase.execute(request).block();

        assertThat(result.isDocumentTypeMatch()).isTrue();
        assertThat(result.isValid()).isTrue();
        assertThat(result.getFieldComparisons()).hasSize(2);
        assertThat(result.getFieldComparisons()).allMatch(fc -> fc.isMatch());
    }

    @Test
    void shouldMatchCedulaNumberWhenDocumentHasThousandsDots() {
        byte[] content = "pdf".getBytes();
        when(classifierGateway.classify(content)).thenReturn(Mono.just(DocumentType.CEDULA));
        when(extractorGateway.extractFields(DocumentType.CEDULA, content)).thenReturn(Mono.just(Map.of(
                "cedula_numero", "1.234.567.890",
                "cedula_nombres", "ANA",
                "cedula_apellidos", "GOMEZ"
        )));

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .expectedType(DocumentType.CEDULA)
                .documentContent(content)
                .numeroDocumento("1234567890")
                .nombreCompleto("Ana Gomez")
                .build();

        DocumentValidationResult result = useCase.execute(request).block();

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldDetectCedulaNumberMismatch() {
        byte[] content = "pdf".getBytes();
        when(classifierGateway.classify(content)).thenReturn(Mono.just(DocumentType.CEDULA));
        when(extractorGateway.extractFields(DocumentType.CEDULA, content)).thenReturn(Mono.just(Map.of(
                "cedula_numero", "999",
                "cedula_nombres", "ANA",
                "cedula_apellidos", "GOMEZ"
        )));

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .expectedType(DocumentType.CEDULA)
                .documentContent(content)
                .numeroDocumento("123456789")
                .nombreCompleto("Ana Gomez")
                .build();

        DocumentValidationResult result = useCase.execute(request).block();

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldMatchRutForJuridicaApplicant() {
        byte[] content = "pdf".getBytes();
        when(classifierGateway.classify(content)).thenReturn(Mono.just(DocumentType.RUT));
        when(extractorGateway.extractFields(DocumentType.RUT, content))
                .thenReturn(Mono.just(Map.of("tipo_contribuyente", "Persona Jurídica")));

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .expectedType(DocumentType.RUT)
                .documentContent(content)
                .applicantType(ApplicantType.JURIDICA)
                .build();

        DocumentValidationResult result = useCase.execute(request).block();

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldFailRutWhenApplicantTypeDoesNotMatchDocument() {
        byte[] content = "pdf".getBytes();
        when(classifierGateway.classify(content)).thenReturn(Mono.just(DocumentType.RUT));
        when(extractorGateway.extractFields(DocumentType.RUT, content))
                .thenReturn(Mono.just(Map.of("tipo_contribuyente", "Persona Jurídica")));

        DocumentValidationRequest request = DocumentValidationRequest.builder()
                .expectedType(DocumentType.RUT)
                .documentContent(content)
                .applicantType(ApplicantType.NATURAL)
                .build();

        DocumentValidationResult result = useCase.execute(request).block();

        assertThat(result.isValid()).isFalse();
    }
}
