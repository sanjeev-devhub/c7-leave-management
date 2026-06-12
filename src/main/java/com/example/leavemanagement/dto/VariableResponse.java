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
@Schema(description = "Camunda process variable")
public class VariableResponse {

    @Schema(example = "decision")
    private String name;

    @Schema(example = "String")
    private String type;

    @Schema(description = "The deserialised value", example = "APPROVED")
    private Object value;

    @Schema(example = "a1b2c3d4-...")
    private String processInstanceId;
}
