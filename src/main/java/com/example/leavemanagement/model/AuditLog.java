package com.example.leavemanagement.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Audit log entry created by {@code LogRequestDelegate} when a leave request is
 * approved. Persisted to Postgres via JPA in the {@code leave_audit_log} table.
 */
@Entity
@Table(name = "leave_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @Column(name = "audit_id", nullable = false, length = 64)
    private String auditId;

    @Column(name = "process_instance_id", length = 64)
    private String processInstanceId;

    @Column(name = "business_key", length = 128)
    private String businessKey;

    @Column(name = "employee_id", length = 64)
    private String employeeId;

    @Column(name = "employee_name", length = 128)
    private String employeeName;

    @Column(name = "leave_type", length = 64)
    private String leaveType;

    @Column(name = "decision", length = 32)
    private String decision;

    @Column(name = "manager_comments", length = 1024)
    private String managerComments;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt;
}
