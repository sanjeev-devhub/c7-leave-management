package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.PagedResponse;
import com.example.leavemanagement.dto.UpdateVariablesRequest;
import com.example.leavemanagement.dto.VariableResponse;
import com.example.leavemanagement.dto.VariableSearchRequest;
import com.example.leavemanagement.service.ProcessService;
import com.example.leavemanagement.service.VariableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for process variables.
 * <pre>
 *   POST /api/variables/search -> RuntimeService.getVariables(pid)
 *   POST /api/variables        -> RuntimeService.setVariables(pid, vars)
 * </pre>
 */
@RestController
@RequestMapping("/api/variables")
@RequiredArgsConstructor
@Tag(name = "Variables", description = "Process variables")
public class VariableController {

    private final VariableService variableService;
    private final ProcessService processService;

    @PostMapping("/search")
    @Operation(summary = "Search process variables for an instance")
    public ResponseEntity<PagedResponse<VariableResponse>> search(@RequestBody @Valid VariableSearchRequest request) {
        return ResponseEntity.ok(variableService.search(request));
    }

    @PostMapping
    @Operation(summary = "Set or update variables on a process instance")
    public ResponseEntity<Void> update(@RequestBody @Valid UpdateVariablesRequest request) {
        processService.updateVariables(request);
        return ResponseEntity.noContent().build();
    }
}
