package com.stg.szp.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stg.szp.models.Project;
import com.stg.szp.models.ProjectFile;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    List<ProjectFile> findAllByProjectId(Long projectId);
    List<ProjectFile> findAllByProjectIn(List<Project> projects);
    
}
