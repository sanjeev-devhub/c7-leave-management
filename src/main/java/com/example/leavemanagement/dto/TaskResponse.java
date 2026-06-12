package com.example.leavemanagement.dto;

import com.example.leavemanagement.model.TaskState;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Representation of a user task, returned by both the search and get-by-id endpoints.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detail of a Camunda user task")
public class TaskResponse {

    @Schema(example = "a1b2c3d4")
    private String taskId;

    @Schema(example = "Review Request")
    private String taskName;

    @Schema(example = "UserTask_ReviewRequest")
    private String taskDefinitionKey;

    @Schema(example = "annual-leave-request")
    private String processDefinitionKey;

    @Schema(example = "a1b2c3d4-...")
    private String processInstanceId;

    @Schema(example = "manager01")
    private String assignee;

    @Schema(example = "CREATED")
    private TaskState state;

    @Schema(example = "[\"line-manager\"]")
    private List<String> candidateGroups;

    @Schema(example = "50")
    private Integer priority;

    private OffsetDateTime created;

    private OffsetDateTime completed;

    private OffsetDateTime due;

    @Schema(description = "Process variables visible to this task scope")
    private Map<String, Object> variables;
}
