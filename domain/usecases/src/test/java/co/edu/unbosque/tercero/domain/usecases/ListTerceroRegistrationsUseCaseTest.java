package co.edu.unbosque.tercero.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class ListTerceroRegistrationsUseCaseTest {

    private final ITerceroRegistrationRepository terceroRegistrationRepository = mock(ITerceroRegistrationRepository.class);
    private final ListTerceroRegistrationsUseCase useCase =
            new ListTerceroRegistrationsUseCase(terceroRegistrationRepository);

    @Test
    void shouldReturnRegistrationsFilteredByEstado() {
        TerceroRegistration registration = TerceroRegistration.builder()
                .id("1").nombre("Ana Gomez").estado(RegistrationStatus.REGISTRADO).build();
        when(terceroRegistrationRepository.findAll(RegistrationStatus.REGISTRADO))
                .thenReturn(Flux.fromIterable(List.of(registration)));

        StepVerifier.create(useCase.execute(RegistrationStatus.REGISTRADO))
                .assertNext(result -> assertThat(result.getNombre()).isEqualTo("Ana Gomez"))
                .verifyComplete();
    }
}
