package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeResultDto {

    private List<AnalyzeDocumentDto> documents;
}
