package com.stg.szp.repos;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import com.stg.szp.models.Milestone;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findAllByProjectIdOrderByDueDateAsc(Long projectId);
}
