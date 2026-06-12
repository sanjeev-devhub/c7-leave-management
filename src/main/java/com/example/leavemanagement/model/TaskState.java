package com.example.leavemanagement.model;

/**
 * Logical task states exposed by our API. Camunda 7 distinguishes between
 * active tasks (retrieved from the runtime TaskService) and completed tasks
 * (retrieved from the HistoryService), which we normalise to these values.
 */
public enum TaskState {
    CREATED,
    ASSIGNED,
    COMPLETED
}
