package co.edu.unbosque.tercero.infrastructure.adapters.azuredocumentintelligence.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * Body returned when polling the {@code Operation-Location} URL of an analyze request.
 * {@code status} is one of {@code notStarted}, {@code running}, {@code succeeded}, {@code failed}.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnalyzeOperationDto {

    private String status;
    private AnalyzeResultDto analyzeResult;
}
