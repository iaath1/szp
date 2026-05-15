package com.stg.szp.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
}
