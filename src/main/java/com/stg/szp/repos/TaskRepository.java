package com.stg.szp.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByAssigneeId(Long useId);
    Long countByAssigneeId(Long useId);
}
