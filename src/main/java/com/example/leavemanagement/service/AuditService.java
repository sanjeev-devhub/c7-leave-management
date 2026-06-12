package com.example.leavemanagement.service;

import com.example.leavemanagement.model.AuditLog;
import com.example.leavemanagement.model.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists and retrieves audit log entries (Postgres-backed via JPA).
 * Populated by {@code LogRequestDelegate} on the service task.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    @Transactional
    public AuditLog record(AuditLog entry) {
        return repository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> all() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public AuditLog get(String auditId) {
        return repository.findById(auditId).orElse(null);
    }
}
