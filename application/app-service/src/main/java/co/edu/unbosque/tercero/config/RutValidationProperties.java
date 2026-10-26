package co.edu.unbosque.tercero.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * The exact {@code tipo_contribuyente} text the RUT extraction model is expected to return for
 * each applicant type, bound from {@code rut-validation.*} in application.yml. Set these to match
 * whatever your custom model actually outputs.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rut-validation")
public class RutValidationProperties {

    private String tipoContribuyenteNatural = "Persona Natural";
    private String tipoContribuyenteJuridica = "Persona Jurídica";
}
