package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Result of starting a leave process instance")
public class StartLeaveResponse {

    @Schema(description = "Camunda process instance id", example = "a1b2c3d4-...")
    private String processInstanceId;

    @Schema(example = "annual-leave-request")
    private String processDefinitionKey;

    @Schema(description = "Business key, set to the employeeId", example = "EMP-001")
    private String businessKey;
}
