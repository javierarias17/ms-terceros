package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the Azure AI Document Intelligence resource (endpoint, key, custom models) from
 * {@code azure.document-intelligence.*} in application.yml. See application.yml.example for the
 * expected shape and where to find each value in the Azure Portal / Document Intelligence Studio.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "azure.document-intelligence")
public class AzureDocumentIntelligenceProperties {

    private String endpoint;
    private String apiKey;
    private String apiVersion = "2024-11-30";

    /**
     * Minimum confidence (0-1) Azure must report for a classification or an extracted field to be
     * trusted. Below this, a classification is treated as {@code UNKNOWN} and a field as not
     * extracted, instead of acting on a low-confidence guess.
     */
    private double minConfidence = 0.85;

    private Classifier classifier = new Classifier();
    private Models models = new Models();
    private Polling polling = new Polling();

    @Getter
    @Setter
    public static class Classifier {
        private String modelId;
        private String labelCedula = "cedula";
        private String labelRut = "rut";
    }

    @Getter
    @Setter
    public static class Models {
        private String cedulaModelId;
        private String rutModelId;
    }

    @Getter
    @Setter
    public static class Polling {
        private long intervalMillis = 2000L;
        private int maxAttempts = 30;
    }
}
