package com.stg.szp.repos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Task;
import java.util.Optional;
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
    boolean existsByProjectIdAndTaskSequence(Long projectId, Integer taskSeq);
    @org.springframework.data.jpa.repository.Query("SELECT MAX(t.taskSequence) FROM Task t WHERE t.project.id = :projectId")
    Integer findMaxTaskSequenceByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);

    // own
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    Long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    Long countByProjectId(Long projectid);
    List<Task> findAllByAssigneeIdAndStatus(Long assigneeId, TaskStatus status);

    @Query(
        "SELECT FUNCTION('DATE', t.completedAt), COUNT(t) FROM Task t " +
        "WHERE t.assignee.id = :userId " +
        "AND t.completedAt >= :startDate " +
        "GROUP BY FUNCTION('DATE', t.completedAt)"
    )
    List<Object[]> countCompletedTasksByDate(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    List<Task> findByAssigneeIdAndDeadlineAtBetween(Long assigneeId, LocalDateTime starTime, LocalDateTime endTime);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignee.id = :assigneeId AND t.status = :status AND t.updatedAt >= :startDate")
    Long countByAssigneeIdAndStatusAndUpdatedAtGreaterThanEqual(
        @Param("assigneeId") Long assigneeId,
        @Param("status") TaskStatus status,
        @Param("startDate") LocalDateTime startDate
    );

    @Query(
        "SELECT FUNCTION('DATE', t.completedAt), COUNT(t) FROM Task t " +
        "WHERE t.project.id = :projectId AND t.completedAt >= :startDate " +
        "GROUP BY FUNCTION('DATE', t.completedAt)"
    )
    List<Object[]> countCompletedTasksByDateAndProject(@Param("projectId") Long projectId, @Param("startDate") LocalDateTime startDate); 
}
