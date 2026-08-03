package com.stg.szp.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.MilestoneDTO;
import com.stg.szp.models.Milestone;
import com.stg.szp.models.MilestoneStatus;
import com.stg.szp.models.Project;
import com.stg.szp.repos.MilestoneRepository;
import com.stg.szp.repos.ProjectRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MilestoneService {
    private final MilestoneRepository milestoneRepo;
    private final ProjectRepository projectRepo;

    @Transactional
    public List<MilestoneDTO> getProjectMilestones(Long projectId) {
        List<Milestone> milestones = milestoneRepo.findAllByProjectIdOrderByDueDateAsc(projectId);
        
        return milestones.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public MilestoneDTO createMilestone(Long projectId, MilestoneDTO req) {
        Project project = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));

        Milestone milestone = Milestone.builder()
            .title(req.getTitle())
            .description(req.getDescription())
            .dueDate(req.getDueDate())
            .status(req.getStatus() == null ? MilestoneStatus.PENDING : req.getStatus())
            .project(project)
            .build();

        Milestone saved = milestoneRepo.save(milestone);
        return mapToDto(saved);
    }


    private MilestoneDTO mapToDto(Milestone milestone) {
        MilestoneDTO dto = new MilestoneDTO();
        dto.setId(milestone.getId());
        dto.setTitle(milestone.getTitle());
        dto.setDescription(milestone.getDescription());
        dto.setDueDate(milestone.getDueDate());
        dto.setStatus(milestone.getStatus());

        if(milestone.getTasks() != null) {
            dto.setTasksTotal(milestone.getTasks().size());
            dto.setTasksCompleted(milestone.getTasks().stream()
                .filter(task -> task.getStatus().equals("DONE")).count()
            );
        } else {
            dto.setTasksTotal(0);
            dto.setTasksCompleted(0L);
        }

        return dto;
    }

}
