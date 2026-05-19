package com.shane.taskdemo.dto;

import com.shane.taskdemo.model.Task;
import jakarta.validation.constraints.NotNull;

public class StatusUpdate {

    @NotNull
    private Task.Status status;

    public Task.Status getStatus() { return status; }
    public void setStatus(Task.Status status) { this.status = status; }
}
