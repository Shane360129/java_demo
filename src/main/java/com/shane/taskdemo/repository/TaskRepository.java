package com.shane.taskdemo.repository;

import com.shane.taskdemo.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Transactional
    long deleteByStatus(Task.Status status);
}
