package com.stg.szp.repos;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stg.szp.models.Subtask;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    
}
