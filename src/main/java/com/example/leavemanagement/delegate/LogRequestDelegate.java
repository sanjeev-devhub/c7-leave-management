package com.example.leavemanagement.delegate;

import com.example.leavemanagement.model.AuditLog;
import com.example.leavemanagement.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Service-task delegate for the "Log Request" step. This is the Camunda 7
 * equivalent of the Camunda 8 {@code log-request-worker} external task worker.
 *
 * <p>Wired into the BPMN via {@code camunda:delegateExpression="${logRequestDelegate}"}.
 * The bean name {@code logRequestDelegate} is the default derived from the class
 * name, so the expression resolves it from the Spring context.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Persist an audit log entry (to Postgres) recording the approval</li>
 *   <li>Write {@code auditId} and {@code loggedAt} back as process variables</li>
 * </ul>
 *
 * <p>The service task is marked {@code camunda:asyncBefore="true"} in the BPMN,
 * so the engine's job executor runs this with transactional retry semantics
 * (default 3 retries), mirroring the Zeebe retry policy.</p>
 */
@Slf4j
@Component("logRequestDelegate")
@RequiredArgsConstructor
public class LogRequestDelegate implements JavaDelegate {

    private final AuditService auditService;

    @Override
    public void execute(DelegateExecution execution) {
        String auditId = "audit-" + UUID.randomUUID();
        Instant loggedAt = Instant.now();

        AuditLog entry = AuditLog.builder()
                .auditId(auditId)
                .processInstanceId(execution.getProcessInstanceId())
                .businessKey(execution.getBusinessKey())
                .employeeId((String) execution.getVariable("employeeId"))
                .employeeName((String) execution.getVariable("employeeName"))
                .leaveType((String) execution.getVariable("leaveType"))
                .decision((String) execution.getVariable("decision"))
                .managerComments((String) execution.getVariable("managerComments"))
                .loggedAt(loggedAt)
                .build();

        auditService.record(entry);

        execution.setVariable("auditId", auditId);
        execution.setVariable("loggedAt", loggedAt.toString());

        log.info("Audit recorded: {}", entry);
    }
}
