package co.edu.unbosque.tercero.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.ThirdParty;
import co.edu.unbosque.tercero.domain.model.exception.ResourceNotFoundException;
import co.edu.unbosque.tercero.domain.ports.IThirdPartyGateway;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class GetThirdPartyUseCaseTest {

    private final IThirdPartyGateway gateway = mock(IThirdPartyGateway.class);
    private final GetThirdPartyUseCase useCase = new GetThirdPartyUseCase(gateway);

    @Test
    void shouldReturnThirdPartyWhenFound() {
        ThirdParty thirdParty = ThirdParty.builder().id("1").name("Acme").build();
        when(gateway.findById("1")).thenReturn(Mono.just(thirdParty));

        StepVerifier.create(useCase.execute("1"))
                .expectNext(thirdParty)
                .verifyComplete();
    }

    @Test
    void shouldRaiseResourceNotFoundWhenGatewayReturnsEmpty() {
        when(gateway.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("missing"))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    void domainModelShouldExposeProvidedFields() {
        ThirdParty thirdParty = ThirdParty.builder()
                .id("1")
                .name("Acme")
                .documentType("NIT")
                .documentNumber("900123456")
                .build();

        assertThat(thirdParty.getId()).isEqualTo("1");
        assertThat(thirdParty.getName()).isEqualTo("Acme");
    }
}
