package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error envelope returned by the API")
public class ErrorResponse {

    @Schema(example = "400")
    private int status;

    @Schema(example = "Bad Request")
    private String error;

    @Schema(example = "Validation failed")
    private String message;

    @Schema(example = "/api/tasks/123/assign")
    private String path;

    private Instant timestamp;

    private List<String> details;

    @Schema(description = "Correlation id used in logs for this request")
    private String correlationId;
}
