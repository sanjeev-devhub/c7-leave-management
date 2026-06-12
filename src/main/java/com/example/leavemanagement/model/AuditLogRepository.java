package com.example.leavemanagement.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByProcessInstanceId(String processInstanceId);

    List<AuditLog> findByEmployeeId(String employeeId);
}
