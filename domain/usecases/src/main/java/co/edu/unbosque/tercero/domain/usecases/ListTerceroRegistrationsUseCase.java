package co.edu.unbosque.tercero.domain.usecases;

import co.edu.unbosque.tercero.domain.model.RegistrationStatus;
import co.edu.unbosque.tercero.domain.model.TerceroRegistration;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * Lists tercero registrations for the admin panel, optionally filtered by {@link RegistrationStatus}.
 */
@RequiredArgsConstructor
public class ListTerceroRegistrationsUseCase {

    private final ITerceroRegistrationRepository terceroRegistrationRepository;

    public Flux<TerceroRegistration> execute(RegistrationStatus estado) {
        return terceroRegistrationRepository.findAll(estado);
    }
}
