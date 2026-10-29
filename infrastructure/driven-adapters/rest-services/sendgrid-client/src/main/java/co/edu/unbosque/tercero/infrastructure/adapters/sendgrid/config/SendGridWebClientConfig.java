package co.edu.unbosque.tercero.infrastructure.adapters.sendgrid.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class SendGridWebClientConfig {

    private static final String SENDGRID_BASE_URL = "https://api.sendgrid.com";

    private final SendGridProperties properties;

    @Bean
    public WebClient sendGridWebClient() {
        return WebClient.builder()
                .baseUrl(SENDGRID_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .build();
    }
}
