package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.AssignTaskRequest;
import com.example.leavemanagement.dto.CompleteTaskRequest;
import com.example.leavemanagement.dto.PagedResponse;
import com.example.leavemanagement.dto.TaskResponse;
import com.example.leavemanagement.dto.TaskSearchRequest;
import com.example.leavemanagement.exception.BadRequestException;
import com.example.leavemanagement.exception.NotFoundException;
import com.example.leavemanagement.mapper.TaskMapper;
import com.example.leavemanagement.model.Decision;
import com.example.leavemanagement.model.TaskState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service over the Camunda 7 {@link TaskService} and {@link HistoryService}.
 *
 * <p>Mapping from the Camunda 8 design to Camunda 7:</p>
 * <pre>
 *   newUserTaskQuery()                 -> taskService.createTaskQuery() (+ historyService for COMPLETED)
 *   newUserTaskGetRequest(id)          -> taskService.createTaskQuery().taskId(id).singleResult()
 *   newUserTaskAssignCommand(id)       -> taskService.setAssignee(id, assignee)
 *   newUserTaskUnassignCommand(id)     -> taskService.setAssignee(id, null)
 *   newUserTaskCompleteCommand(id)     -> taskService.complete(id, variables)
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTaskService {

    private final TaskService taskService;
    private final HistoryService historyService;
    private final TaskMapper taskMapper;

    public PagedResponse<TaskResponse> search(TaskSearchRequest filter) {
        int pageSize = filter.getPageSize() != null ? filter.getPageSize() : 20;
        int firstResult = filter.getFirstResult() != null ? filter.getFirstResult() : 0;

        // COMPLETED tasks live in history; everything else is a runtime query.
        if (filter.getState() == TaskState.COMPLETED) {
            return searchHistory(filter, firstResult, pageSize);
        }
        return searchRuntime(filter, firstResult, pageSize);
    }

    private PagedResponse<TaskResponse> searchRuntime(TaskSearchRequest filter, int firstResult, int pageSize) {
        TaskQuery query = taskService.createTaskQuery();

        if (filter.getAssignee() != null) {
            query.taskAssignee(filter.getAssignee());
        }
        if (filter.getCandidateGroup() != null) {
            // Note: by default taskCandidateGroup() excludes tasks already claimed
            // by a user (they leave the group queue). Add includeAssignedTasks()
            // when an explicit assignee filter is also present so both can combine.
            query.taskCandidateGroup(filter.getCandidateGroup());
            if (filter.getAssignee() != null || Boolean.TRUE.equals(filter.getAssigned())) {
                query.includeAssignedTasks();
            }
        }
        if (filter.getProcessInstanceId() != null) {
            query.processInstanceId(filter.getProcessInstanceId());
        }
        if (filter.getPriority() != null) {
            query.taskPriority(filter.getPriority());
        }
        if (Boolean.TRUE.equals(filter.getAssigned())) {
            query.taskAssigned();
        } else if (Boolean.FALSE.equals(filter.getAssigned())) {
            query.taskUnassigned();
        }
        // ASSIGNED state implies an assignee must be present
        if (filter.getState() == TaskState.ASSIGNED) {
            query.taskAssigned();
        }

        long total = query.count();
        List<Task> tasks = query.orderByTaskCreateTime().desc()
                .listPage(firstResult, pageSize);

        List<TaskResponse> items = tasks.stream()
                .map(t -> taskMapper.toTaskResponse(t, taskService.getVariables(t.getId())))
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .items(items).total(total).firstResult(firstResult).pageSize(pageSize).build();
    }

    private PagedResponse<TaskResponse> searchHistory(TaskSearchRequest filter, int firstResult, int pageSize) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery().finished();

        if (filter.getAssignee() != null) {
            query.taskAssignee(filter.getAssignee());
        }
        if (filter.getProcessInstanceId() != null) {
            query.processInstanceId(filter.getProcessInstanceId());
        }
        if (filter.getPriority() != null) {
            query.taskPriority(filter.getPriority());
        }

        long total = query.count();
        List<HistoricTaskInstance> tasks = query.orderByHistoricTaskInstanceEndTime().desc()
                .listPage(firstResult, pageSize);

        List<TaskResponse> items = tasks.stream()
                .map(taskMapper::toTaskResponse)
                .toList();

        return PagedResponse.<TaskResponse>builder()
                .items(items).total(total).firstResult(firstResult).pageSize(pageSize).build();
    }

    public TaskResponse get(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            // Maybe it's already completed - check history
            HistoricTaskInstance historic = historyService.createHistoricTaskInstanceQuery()
                    .taskId(taskId).singleResult();
            if (historic == null) {
                throw new NotFoundException("Task not found: " + taskId);
            }
            return taskMapper.toTaskResponse(historic);
        }
        return taskMapper.toTaskResponse(task, taskService.getVariables(taskId));
    }

    public void assign(String taskId, AssignTaskRequest request) {
        requireActiveTask(taskId);
        taskService.setAssignee(taskId, request.getAssignee());
        log.info("Assigned task {} to {}", taskId, request.getAssignee());
    }

    public void unassign(String taskId) {
        requireActiveTask(taskId);
        taskService.setAssignee(taskId, null);
        log.info("Unassigned task {}", taskId);
    }

    /**
     * Complete a task. For Review Request, validate that {@code decision} is one
     * of the allowed {@link Decision} values before completing.
     */
    public void complete(String taskId, CompleteTaskRequest request) {
        Task task = requireActiveTask(taskId);
        Map<String, Object> vars = request.getVariables();

        if ("UserTask_ReviewRequest".equals(task.getTaskDefinitionKey())) {
            validateDecision(vars);
        }

        if (vars == null || vars.isEmpty()) {
            taskService.complete(taskId);
        } else {
            taskService.complete(taskId, vars);
        }
        log.info("Completed task {} ({})", taskId, task.getName());
    }

    private Task requireActiveTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new NotFoundException("Active task not found: " + taskId);
        }
        return task;
    }

    private void validateDecision(Map<String, Object> variables) {
        if (variables == null || !variables.containsKey("decision")) {
            throw new BadRequestException("'decision' variable is required to complete Review Request");
        }
        Object raw = variables.get("decision");
        if (raw == null) {
            throw new BadRequestException("'decision' must not be null");
        }
        try {
            Decision.valueOf(raw.toString());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "'decision' must be one of APPROVED / REJECTED / MORE_INFO_REQUIRED but was: " + raw);
        }
    }

    public List<TaskResponse> getAll() {
        List<Task> tasks= taskService.createTaskQuery().active().list();
        if (tasks == null) {
            // Maybe it's already completed - check history
            throw new NotFoundException("Task not found: ");

        }
        List<TaskResponse> tasksDto = new ArrayList<>();
        for (Task task:tasks){
            tasksDto.add(taskMapper.toTaskResponse(task, taskService.getVariables(task.getId())));
        }

        return tasksDto;
    }
    public List<TaskResponse> getAllHistoric() {
        List<TaskResponse> tasksDto = new ArrayList<>();
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey("annual-leave-request").list();
        if (tasks == null) {
            // Maybe it's already completed - check history
            throw new NotFoundException("Task not found: ");

        }
        for (HistoricTaskInstance task:tasks){
            tasksDto.add(taskMapper.toTaskResponse(task));
        }

        return tasksDto;
    }
}
