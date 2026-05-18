package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.ProjectResponseDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<ProjectResponseDTO> getUserProjects(SZP_User user) {
        return projectRepository.findAllByOwnerOrMember(user).stream()
                .map(project -> ProjectResponseDTO.builder()
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .createdAt(project.getCreatedAt())
                        .owner(project.getOwner())
                        .build())
                .toList();
    }

    public ProjectResponseDTO createProject(SZP_User user, String title, String description) {
        Project project = new Project();
        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(user);
        project.setDescription(description);
        project.setTitle(title);

        projectRepository.save(project);

        return ProjectResponseDTO.builder()
                .createdAt(project.getCreatedAt())
                .description(description)
                .title(title)
                .owner(user)
                .build();
    }

}
