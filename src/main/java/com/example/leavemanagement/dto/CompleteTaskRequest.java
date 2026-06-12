package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to complete a task with variables")
public class CompleteTaskRequest {

    @Schema(
        description = "Variables to set on completion. For Review Request these should include 'decision' "
            + "(APPROVED / REJECTED / MORE_INFO_REQUIRED) and 'managerComments'.",
        example = "{\"decision\":\"APPROVED\",\"managerComments\":\"Approved\"}")
    private Map<String, Object> variables;
}
