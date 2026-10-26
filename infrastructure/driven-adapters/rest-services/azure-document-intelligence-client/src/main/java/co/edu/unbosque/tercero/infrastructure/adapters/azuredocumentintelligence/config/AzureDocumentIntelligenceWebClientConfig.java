package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class AzureDocumentIntelligenceWebClientConfig {

    private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";

    private final AzureDocumentIntelligenceProperties properties;

    @Bean
    public WebClient azureDocumentIntelligenceWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getEndpoint())
                .defaultHeader(SUBSCRIPTION_KEY_HEADER, properties.getApiKey())
                .build();
    }
}
