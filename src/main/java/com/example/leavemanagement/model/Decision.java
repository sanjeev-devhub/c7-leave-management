package com.example.leavemanagement.model;

/**
 * Possible decisions a line manager can take when reviewing a leave request.
 * Drives the exclusive gateway via the process variable {@code decision}.
 */
public enum Decision {
    APPROVED,
    REJECTED,
    MORE_INFO_REQUIRED
}
