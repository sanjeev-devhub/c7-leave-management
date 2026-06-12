package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.PagedResponse;
import com.example.leavemanagement.dto.VariableResponse;
import com.example.leavemanagement.dto.VariableSearchRequest;
import com.example.leavemanagement.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Reads process variables for a given instance via {@link RuntimeService}.
 * Replaces the Camunda 8 {@code newVariablesGetRequest()} call.
 */
@Service
@RequiredArgsConstructor
public class VariableService {

    private final RuntimeService runtimeService;

    public PagedResponse<VariableResponse> search(VariableSearchRequest request) {
        String pid = request.getProcessInstanceId();

        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(pid).singleResult();
        if (instance == null) {
            throw new NotFoundException("Active process instance not found: " + pid);
        }

        Map<String, Object> all = runtimeService.getVariables(pid);

        List<VariableResponse> items = all.entrySet().stream()
                .filter(e -> request.getName() == null || request.getName().equals(e.getKey()))
                .map(e -> VariableResponse.builder()
                        .name(e.getKey())
                        .type(e.getValue() != null ? e.getValue().getClass().getSimpleName() : "null")
                        .value(e.getValue())
                        .processInstanceId(pid)
                        .build())
                .toList();

        return PagedResponse.<VariableResponse>builder()
                .items(items).total((long) items.size()).firstResult(0).pageSize(items.size()).build();
    }
}
