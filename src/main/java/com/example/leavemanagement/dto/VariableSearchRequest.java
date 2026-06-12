package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter for variable search")
public class VariableSearchRequest {

    @NotBlank
    @Schema(example = "a1b2c3d4-...", description = "Process instance id to read variables from")
    private String processInstanceId;

    @Schema(description = "Optional single variable name filter")
    private String name;
}
