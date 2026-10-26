package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * One entry of {@code analyzeResult.documents[].fields} in the Azure Document Intelligence
 * response. {@code content} holds the raw recognized text regardless of the field's value type,
 * which is all this adapter needs.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentFieldDto {

    private String type;
    private String content;
    private Double confidence;
}
