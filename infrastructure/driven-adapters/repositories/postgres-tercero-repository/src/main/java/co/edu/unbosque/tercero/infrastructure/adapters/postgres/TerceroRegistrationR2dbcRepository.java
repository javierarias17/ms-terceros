package co.edu.unbosque.tercero.infrastructure.adapters.postgres;

import co.edu.unbosque.tercero.infrastructure.adapters.postgres.entity.TerceroRegistrationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TerceroRegistrationR2dbcRepository extends ReactiveCrudRepository<TerceroRegistrationEntity, String> {
}
