package com.stg.szp.repos;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Task;
import com.stg.szp.models.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByAssigneeId(Long useId);
    Long countByAssigneeId(Long useId);
    Long countByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);
    List<Task> findAllByDeadlineAtBeforeAndStatusNot(LocalDateTime deadline, TaskStatus status);
    List<Task> findTop5ByAssigneeIdOrderByDeadlineAtAsc(Long assigneeId);
    List<Task> findTop5ByProjectIdOrderByDeadlineAtAsc(Long projectId);
    List<Task> findAllByProjectId(Long projectId);

    // own
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    Long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    Long countByProjectId(Long projectid);
}
