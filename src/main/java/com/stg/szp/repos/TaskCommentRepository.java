package com.stg.szp.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.TaskComment;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskComment, Long>{
    List<TaskComment> findAllByTaskIdOrderByCreatedAtAsc(Long taskId);
}
