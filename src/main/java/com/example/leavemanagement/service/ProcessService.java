package com.example.leavemanagement.service;

import com.example.leavemanagement.dto.StartLeaveRequest;
import com.example.leavemanagement.dto.StartLeaveResponse;
import com.example.leavemanagement.dto.UpdateVariablesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Orchestrates process-level operations against the embedded Camunda 7 engine:
 * starting an instance and setting variables on a running instance.
 *
 * <p>This replaces the Zeebe {@code newCreateInstanceCommand()} /
 * {@code newSetVariablesCommand()} calls from the Camunda 8 version with the
 * in-JVM {@link RuntimeService} API.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessService {

    public static final String PROCESS_KEY = "annual-leave-request";

    private final RuntimeService runtimeService;

    /**
     * Start an Annual Leave process instance. The employeeId is used as the
     * business key so instances are easy to correlate. Initial variables match
     * the form fields of the "Request Annual Leave" user task.
     */
    public StartLeaveResponse startLeaveRequest(StartLeaveRequest request) {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("employeeId", request.getEmployeeId());
        vars.put("employeeName", request.getEmployeeName());
        vars.put("leaveType", request.getLeaveType());
        vars.put("startDate", request.getStartDate() != null ? request.getStartDate().toString() : null);
        vars.put("endDate", request.getEndDate() != null ? request.getEndDate().toString() : null);
        vars.put("reason", request.getReason());
        vars.put("daysRequested", request.getDaysRequested());

        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                PROCESS_KEY, request.getEmployeeId(), vars);

        log.info("Started process instance {} (businessKey={})",
                instance.getProcessInstanceId(), instance.getBusinessKey());

        return StartLeaveResponse.builder()
                .processInstanceId(instance.getProcessInstanceId())
                .processDefinitionKey(PROCESS_KEY)
                .businessKey(instance.getBusinessKey())
                .build();
    }

    /**
     * Set variables on a running process instance. Equivalent to the Camunda 8
     * "set variables" command — here it's {@code RuntimeService.setVariables}.
     */
    public void updateVariables(UpdateVariablesRequest request) {
        runtimeService.setVariables(request.getProcessInstanceId(), request.getVariables());
        log.info("Updated {} variables on instance {}",
                request.getVariables().size(), request.getProcessInstanceId());
    }
}
