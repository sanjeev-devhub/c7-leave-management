package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.AssignTaskRequest;
import com.example.leavemanagement.dto.CompleteTaskRequest;
import com.example.leavemanagement.dto.PagedResponse;
import com.example.leavemanagement.dto.TaskResponse;
import com.example.leavemanagement.dto.TaskSearchRequest;
import com.example.leavemanagement.service.UserTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for Camunda 7 user tasks. Same endpoints as the Camunda 8 version,
 * backed by the in-JVM TaskService/HistoryService:
 * <pre>
 *   POST /api/tasks/search            -> TaskService.createTaskQuery()
 *   GET  /api/tasks/{taskId}          -> taskQuery().taskId(id)
 *   POST /api/tasks/{taskId}/assign   -> TaskService.setAssignee(id, x)
 *   POST /api/tasks/{taskId}/unassign -> TaskService.setAssignee(id, null)
 *   POST /api/tasks/{taskId}/complete -> TaskService.complete(id, vars)
 * </pre>
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "User task operations on the leave-request process")
public class TaskController {

    private final UserTaskService userTaskService;

    @PostMapping("/search")
    @Operation(summary = "Search user tasks")
    public ResponseEntity<PagedResponse<TaskResponse>> search(@RequestBody @Valid TaskSearchRequest request) {
        return ResponseEntity.ok(userTaskService.search(request));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by id")
    public ResponseEntity<TaskResponse> get(@PathVariable String taskId) {
        return ResponseEntity.ok(userTaskService.get(taskId));
    }
    @GetMapping("/all")
    @Operation(summary = "Get All Active tasks")
    public ResponseEntity<List<TaskResponse>> getAll() {
        return ResponseEntity.ok(userTaskService.getAll());
    }
    @GetMapping("/all/historic")
    @Operation(summary = "Get All Active tasks")
    public ResponseEntity<List<TaskResponse>> getAllHistoric() {
        return ResponseEntity.ok(userTaskService.getAllHistoric());
    }

    @PostMapping("/{taskId}/assign")
    @Operation(summary = "Assign task to a user")
    public ResponseEntity<Void> assign(@PathVariable String taskId, @RequestBody @Valid AssignTaskRequest request) {
        userTaskService.assign(taskId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/unassign")
    @Operation(summary = "Unassign task")
    public ResponseEntity<Void> unassign(@PathVariable String taskId) {
        userTaskService.unassign(taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/complete")
    @Operation(summary = "Complete task with variables",
            description = "For Review Request, 'decision' must be APPROVED, REJECTED or MORE_INFO_REQUIRED.")
    public ResponseEntity<Void> complete(@PathVariable String taskId, @RequestBody CompleteTaskRequest request) {
        userTaskService.complete(taskId, request);
        return ResponseEntity.noContent().build();
    }
}
