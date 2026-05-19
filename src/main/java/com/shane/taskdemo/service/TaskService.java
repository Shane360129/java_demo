package com.shane.taskdemo.service;

import com.shane.taskdemo.dto.TaskRequest;
import com.shane.taskdemo.model.Task;
import com.shane.taskdemo.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Task not found: " + id);
        }
        repository.deleteById(id);
    }

    private void apply(Task task, TaskRequest req) {
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());
        task.setStatus(req.getStatus() != null ? req.getStatus() : Task.Status.TODO);
        task.setDueDate(req.getDueDate());
    }
}
