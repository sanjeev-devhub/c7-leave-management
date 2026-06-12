package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to set or update variables on a process instance")
public class UpdateVariablesRequest {

    @NotBlank
    @Schema(example = "a1b2c3d4-...", description = "Process instance id (execution scope)")
    private String processInstanceId;

    @NotEmpty
    @Schema(example = "{\"additionalInformation\":\"Attached updated document\"}")
    private Map<String, Object> variables;
}
