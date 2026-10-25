package co.edu.unbosque.tercero.config;

import co.edu.unbosque.tercero.domain.ports.IThirdPartyGateway;
import co.edu.unbosque.tercero.domain.usecases.GetThirdPartyUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean definitions for the use cases of this microservice.
 * <p>
 * Use cases are plain classes (no {@code @Service}, no interface), so each one is registered
 * here as a {@code @Bean} receiving its output ports by constructor. Add one {@code @Bean} per
 * use case as it is implemented.
 * </p>
 */
@Configuration
public class UseCaseBeanConfig {

    @Bean
    public GetThirdPartyUseCase getThirdPartyUseCase(IThirdPartyGateway thirdPartyGateway) {
        return new GetThirdPartyUseCase(thirdPartyGateway);
    }
}
