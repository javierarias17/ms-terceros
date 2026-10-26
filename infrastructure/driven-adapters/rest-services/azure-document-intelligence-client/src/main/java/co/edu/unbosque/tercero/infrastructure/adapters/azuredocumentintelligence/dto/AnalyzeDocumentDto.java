package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeDocumentDto {

    private String docType;
    private Double confidence;
    private Map<String, DocumentFieldDto> fields;
}
