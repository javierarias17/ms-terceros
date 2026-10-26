package co.edu.unbosque.tercero.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import co.edu.unbosque.tercero.domain.ports.IDocumentClassifierGateway;
import co.edu.unbosque.tercero.domain.ports.IDocumentExtractorGateway;
import co.edu.unbosque.tercero.domain.usecases.ValidateDocumentUseCase;
import org.junit.jupiter.api.Test;

class UseCaseBeanConfigTest {

    private final UseCaseBeanConfig config = new UseCaseBeanConfig(new RutValidationProperties());

    @Test
    void validateDocumentUseCaseBeanShouldBeCreated() {
        ValidateDocumentUseCase bean = config.validateDocumentUseCase(
                mock(IDocumentClassifierGateway.class), mock(IDocumentExtractorGateway.class));

        assertThat(bean).isNotNull();
    }
}
