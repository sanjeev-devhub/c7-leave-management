package com.example.leavemanagement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request payload to start a new Annual Leave process instance. Populates the
 * initial process variables consumed by the "Request Annual Leave" user task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payload for starting a new annual leave process instance")
public class StartLeaveRequest {

    @NotBlank
    @Schema(example = "EMP-001")
    private String employeeId;

    @NotBlank
    @Schema(example = "John Doe")
    private String employeeName;

    @NotBlank
    @Schema(example = "ANNUAL_LEAVE")
    private String leaveType;

    @NotNull
    @Schema(example = "2026-06-01")
    private LocalDate startDate;

    @NotNull
    @Schema(example = "2026-06-05")
    private LocalDate endDate;

    @Schema(example = "Family vacation")
    private String reason;

    @Min(1)
    @Schema(example = "5")
    private Integer daysRequested;
}
