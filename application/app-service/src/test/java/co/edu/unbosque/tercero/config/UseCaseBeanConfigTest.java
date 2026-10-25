package co.edu.unbosque.tercero.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import co.edu.unbosque.tercero.domain.ports.IThirdPartyGateway;
import co.edu.unbosque.tercero.domain.usecases.GetThirdPartyUseCase;
import org.junit.jupiter.api.Test;

class UseCaseBeanConfigTest {

    private final UseCaseBeanConfig config = new UseCaseBeanConfig();

    @Test
    void getThirdPartyUseCaseBeanShouldBeCreated() {
        GetThirdPartyUseCase bean = config.getThirdPartyUseCase(mock(IThirdPartyGateway.class));

        assertThat(bean).isNotNull();
    }
}
