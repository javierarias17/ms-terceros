package co.edu.unbosque.tercero.infrastructure.adapters.thirdparty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the base URL of the external service this adapter calls.
 * Set {@code thirdparty.client.base-url} in application.yml (or an env var override).
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "thirdparty.client")
public class ThirdPartyClientProperties {

    private String baseUrl;
}
