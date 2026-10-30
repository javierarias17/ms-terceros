package co.edu.unbosque.tercero.config;

import co.edu.unbosque.tercero.domain.ports.IDocumentClassifierGateway;
import co.edu.unbosque.tercero.domain.ports.IDocumentExtractorGateway;
import co.edu.unbosque.tercero.domain.ports.IDocumentStorageGateway;
import co.edu.unbosque.tercero.domain.ports.IEmailNotificationGateway;
import co.edu.unbosque.tercero.domain.ports.ITerceroRegistrationRepository;
import co.edu.unbosque.tercero.domain.usecases.FinalizeTerceroRegistrationUseCase;
import co.edu.unbosque.tercero.domain.usecases.GetTerceroRegistrationUseCase;
import co.edu.unbosque.tercero.domain.usecases.ListTerceroRegistrationsUseCase;
import co.edu.unbosque.tercero.domain.usecases.SendRegistrationConfirmationUseCase;
import co.edu.unbosque.tercero.domain.usecases.UpdateTerceroRegistrationStatusUseCase;
import co.edu.unbosque.tercero.domain.usecases.ValidateDocumentUseCase;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UseCaseBeanConfig {

    private final RutValidationProperties rutValidationProperties;

    @Bean
    public ValidateDocumentUseCase validateDocumentUseCase(
            IDocumentClassifierGateway documentClassifierGateway,
            IDocumentExtractorGateway documentExtractorGateway) {
        return new ValidateDocumentUseCase(
                documentClassifierGateway,
                documentExtractorGateway,
                rutValidationProperties.getTipoContribuyenteNatural(),
                rutValidationProperties.getTipoContribuyenteJuridica());
    }

    @Bean
    public SendRegistrationConfirmationUseCase sendRegistrationConfirmationUseCase(
            IEmailNotificationGateway emailNotificationGateway) {
        return new SendRegistrationConfirmationUseCase(emailNotificationGateway);
    }

    @Bean
    public FinalizeTerceroRegistrationUseCase finalizeTerceroRegistrationUseCase(
            IDocumentStorageGateway documentStorageGateway,
            ITerceroRegistrationRepository terceroRegistrationRepository,
            SendRegistrationConfirmationUseCase sendRegistrationConfirmationUseCase) {
        return new FinalizeTerceroRegistrationUseCase(
                documentStorageGateway, terceroRegistrationRepository, sendRegistrationConfirmationUseCase);
    }

    @Bean
    public ListTerceroRegistrationsUseCase listTerceroRegistrationsUseCase(
            ITerceroRegistrationRepository terceroRegistrationRepository) {
        return new ListTerceroRegistrationsUseCase(terceroRegistrationRepository);
    }

    @Bean
    public GetTerceroRegistrationUseCase getTerceroRegistrationUseCase(
            ITerceroRegistrationRepository terceroRegistrationRepository,
            IDocumentStorageGateway documentStorageGateway) {
        return new GetTerceroRegistrationUseCase(terceroRegistrationRepository, documentStorageGateway);
    }

    @Bean
    public UpdateTerceroRegistrationStatusUseCase updateTerceroRegistrationStatusUseCase(
            ITerceroRegistrationRepository terceroRegistrationRepository,
            IEmailNotificationGateway emailNotificationGateway) {
        return new UpdateTerceroRegistrationStatusUseCase(terceroRegistrationRepository, emailNotificationGateway);
    }
}
