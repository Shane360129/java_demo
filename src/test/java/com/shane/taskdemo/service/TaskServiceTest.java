package com.shane.taskdemo.service;

import com.shane.taskdemo.dto.TaskRequest;
import com.shane.taskdemo.model.Task;
import com.shane.taskdemo.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository repository;

    @InjectMocks
    TaskService service;

    TaskRequest req;

    @BeforeEach
    void setUp() {
        req = new TaskRequest();
        req.setTitle("寄出履歷");
        req.setDescription("投三家");
        req.setStatus(Task.Status.TODO);
        req.setPriority(Task.Priority.HIGH);
        req.setDueDate(LocalDate.of(2026, 6, 1));
    }

    @Test
    void create_appliesAllFields() {
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task saved = service.create(req);

        assertThat(saved.getTitle()).isEqualTo("寄出履歷");
        assertThat(saved.getDescription()).isEqualTo("投三家");
        assertThat(saved.getStatus()).isEqualTo(Task.Status.TODO);
        assertThat(saved.getPriority()).isEqualTo(Task.Priority.HIGH);
        assertThat(saved.getDueDate()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(saved.getCompletedAt()).isNull();
    }

    @Test
    void create_defaultsPriorityToMedium_whenRequestPriorityIsNull() {
        req.setPriority(null);
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task saved = service.create(req);

        assertThat(saved.getPriority()).isEqualTo(Task.Priority.MEDIUM);
    }

    @Test
    void updateStatus_toDone_stampsCompletedAt() {
        Task existing = new Task();
        existing.setId(1L);
        existing.setTitle("任務");
        existing.setStatus(Task.Status.IN_PROGRESS);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = service.updateStatus(1L, Task.Status.DONE);

        assertThat(updated.getStatus()).isEqualTo(Task.Status.DONE);
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    @Test
    void updateStatus_leavingDone_clearsCompletedAt() {
        Task existing = new Task();
        existing.setId(2L);
        existing.setTitle("任務");
        existing.setStatus(Task.Status.DONE);
        existing.setCompletedAt(java.time.LocalDateTime.now());
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task updated = service.updateStatus(2L, Task.Status.TODO);

        assertThat(updated.getStatus()).isEqualTo(Task.Status.TODO);
        assertThat(updated.getCompletedAt()).isNull();
    }

    @Test
    void delete_missingId_throwsAndSkipsRepository() {
        when(repository.existsById(42L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("42");

        verify(repository, never()).deleteById(any());
    }

    @Test
    void findById_missing_throws() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteCompleted_delegatesToRepository() {
        when(repository.deleteByStatus(Task.Status.DONE)).thenReturn(3L);

        long removed = service.deleteCompleted();

        assertThat(removed).isEqualTo(3L);
    }
}
