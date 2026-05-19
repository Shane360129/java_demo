package com.shane.taskdemo.service;

import com.shane.taskdemo.dto.TaskRequest;
import com.shane.taskdemo.model.Task;
import com.shane.taskdemo.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found: " + id));
    }

    @Transactional
    public Task create(TaskRequest req) {
        Task task = new Task();
        apply(task, req);
        return repository.save(task);
    }

    @Transactional
    public Task update(Long id, TaskRequest req) {
        Task task = findById(id);
        apply(task, req);
        return repository.save(task);
    }

    @Transactional
    public Task updateStatus(Long id, Task.Status status) {
        Task task = findById(id);
        applyStatus(task, status);
        return repository.save(task);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Task not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public long deleteCompleted() {
        return repository.deleteByStatus(Task.Status.DONE);
    }

    private void apply(Task task, TaskRequest req) {
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setPriority(req.getPriority() != null ? req.getPriority() : Task.Priority.MEDIUM);
        task.setDueDate(req.getDueDate());
        applyStatus(task, req.getStatus() != null ? req.getStatus() : Task.Status.TODO);
    }

    private void applyStatus(Task task, Task.Status newStatus) {
        boolean wasDone = task.getStatus() == Task.Status.DONE;
        boolean nowDone = newStatus == Task.Status.DONE;
        if (nowDone && !wasDone) {
            task.setCompletedAt(LocalDateTime.now());
        } else if (!nowDone) {
            task.setCompletedAt(null);
        }
        task.setStatus(newStatus);
    }
}
