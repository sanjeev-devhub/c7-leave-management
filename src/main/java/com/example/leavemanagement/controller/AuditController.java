package com.example.leavemanagement.controller;

import com.example.leavemanagement.model.AuditLog;
import com.example.leavemanagement.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit logs produced by the Log Request delegate")
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @Operation(summary = "List all audit log entries")
    public ResponseEntity<List<AuditLog>> all() {
        return ResponseEntity.ok(auditService.all());
    }

    @GetMapping("/{auditId}")
    @Operation(summary = "Get audit entry by id")
    public ResponseEntity<AuditLog> get(@PathVariable String auditId) {
        AuditLog entry = auditService.get(auditId);
        return entry == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entry);
    }
}
