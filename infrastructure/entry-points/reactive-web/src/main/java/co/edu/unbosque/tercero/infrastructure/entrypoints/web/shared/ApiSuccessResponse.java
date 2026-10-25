package co.edu.unbosque.tercero.infrastructure.entrypoints.web.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApiSuccessResponse<T> {

    private int statusCode;
    private String status;
    private T data;
    private String timestamp;
    private String transactionId;
}
