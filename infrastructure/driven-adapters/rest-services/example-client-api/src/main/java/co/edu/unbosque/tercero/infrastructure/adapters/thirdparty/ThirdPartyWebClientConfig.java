package co.edu.unbosque.tercero.infrastructure.adapters.thirdparty;

import co.edu.unbosque.tercero.infrastructure.adapters.thirdparty.config.ThirdPartyClientProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class ThirdPartyWebClientConfig {

    private final ThirdPartyClientProperties properties;

    @Bean
    public WebClient thirdPartyWebClient() {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
