package com.example.leavemanagement.controller;

import com.example.leavemanagement.dto.StartLeaveRequest;
import com.example.leavemanagement.dto.StartLeaveResponse;
import com.example.leavemanagement.service.ProcessService;
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
 * Endpoint for starting new instances of the Annual Leave process.
 */
@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@Tag(name = "Leave Requests", description = "Start new leave process instances")
public class LeaveController {

    private final ProcessService processService;

    @PostMapping("/start")
    @Operation(summary = "Start a new Annual Leave process instance")
    public ResponseEntity<StartLeaveResponse> start(@RequestBody @Valid StartLeaveRequest request) {
        return ResponseEntity.ok(processService.startLeaveRequest(request));
    }
}
