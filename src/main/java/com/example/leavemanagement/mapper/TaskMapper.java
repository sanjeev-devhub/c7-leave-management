package com.example.leavemanagement.mapper;

import com.example.leavemanagement.dto.TaskResponse;
import com.example.leavemanagement.model.TaskState;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Converts Camunda 7 {@link Task} (runtime) and {@link HistoricTaskInstance}
 * (history) objects into the API's {@link TaskResponse}.
 *
 * <p>Implemented by hand rather than via MapStruct because we (a) merge in a
 * separately-fetched variables map and (b) resolve candidate groups through the
 * task's identity links, neither of which maps cleanly with annotations.</p>
 */
@Component
public class TaskMapper {

    private final TaskService taskService;

    public TaskMapper(TaskService taskService) {
        this.taskService = taskService;
    }

    /** Runtime task → response, with variables and candidate groups resolved. */
    public TaskResponse toTaskResponse(Task task, Map<String, Object> variables) {
        TaskState state = task.getAssignee() != null ? TaskState.ASSIGNED : TaskState.CREATED;

        return TaskResponse.builder()
                .taskId(task.getId())
                .taskName(task.getName())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .processDefinitionKey(extractProcessKey(task.getProcessDefinitionId()))
                .processInstanceId(task.getProcessInstanceId())
                .assignee(task.getAssignee())
                .state(state)
                .candidateGroups(resolveCandidateGroups(task.getId()))
                .priority(task.getPriority())
                .created(toOffset(task.getCreateTime()))
                .due(toOffset(task.getDueDate()))
                .variables(variables)
                .build();
    }

    /** Historic (completed) task → response. */
    public TaskResponse toTaskResponse(HistoricTaskInstance task) {
        return TaskResponse.builder()
                .taskId(task.getId())
                .taskName(task.getName())
                .taskDefinitionKey(task.getTaskDefinitionKey())
                .processDefinitionKey(extractProcessKey(task.getProcessDefinitionId()))
                .processInstanceId(task.getProcessInstanceId())
                .assignee(task.getAssignee())
                .state(TaskState.COMPLETED)
                .priority(task.getPriority())
                .created(toOffset(task.getStartTime()))
                .completed(toOffset(task.getEndTime()))
                .due(toOffset(task.getDueDate()))
                .build();
    }

    private List<String> resolveCandidateGroups(String taskId) {
        return taskService.getIdentityLinksForTask(taskId).stream()
                .filter(link -> IdentityLinkType.CANDIDATE.equals(link.getType()))
                .map(IdentityLink::getGroupId)
                .filter(g -> g != null)
                .toList();
    }

    /** Camunda process definition ids look like "annual-leave-request:1:abc"; take the key. */
    private String extractProcessKey(String processDefinitionId) {
        if (processDefinitionId == null) {
            return null;
        }
        int idx = processDefinitionId.indexOf(':');
        return idx > 0 ? processDefinitionId.substring(0, idx) : processDefinitionId;
    }

    private OffsetDateTime toOffset(Date date) {
        return date == null ? null : date.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }
}
