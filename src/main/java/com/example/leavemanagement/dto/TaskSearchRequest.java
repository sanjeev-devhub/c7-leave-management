package com.example.leavemanagement.dto;

import com.example.leavemanagement.model.TaskState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter criteria for searching user tasks. Maps to a Camunda 7
 * {@code TaskService.createTaskQuery()} with the relevant filters applied.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter criteria for searching user tasks")
public class TaskSearchRequest {

    @Schema(example = "CREATED", description = "CREATED/ASSIGNED filter active tasks; COMPLETED queries history")
    private TaskState state;

    @Schema(example = "manager01")
    private String assignee;

    @Schema(example = "line-manager")
    private String candidateGroup;

    @Schema(description = "Filter by process instance id")
    private String processInstanceId;

    @Schema(description = "true = only assigned tasks, false = only unassigned")
    private Boolean assigned;

    @Schema(example = "20", defaultValue = "20")
    private Integer pageSize;

    @Schema(example = "0", defaultValue = "0", description = "Zero-based result offset")
    private Integer firstResult;

    @Schema(description = "Filter by task priority")
    private Integer priority;
}
