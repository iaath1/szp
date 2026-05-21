package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.EditProjectDTO;
import com.stg.szp.DTO.ProjectDetailsDTO;
import com.stg.szp.DTO.ProjectResponseDTO;
import com.stg.szp.DTO.UserResponseDTO;
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
                        .owner(mapToUserResponseDTO(project.getOwner()))
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
                .owner(mapToUserResponseDTO(user))
                .build();
    }

    public ProjectResponseDTO changeProject(SZP_User user, EditProjectDTO editProjectDTO, Long projectToBeChangedId) throws Exception {
        Project project = projectRepository.findById(projectToBeChangedId)
                .orElseThrow(() -> new Exception("Project not found"));

        project.setTitle(editProjectDTO.getTitle());
        project.setDescription(editProjectDTO.getDescription());

        projectRepository.save(project);

        return ProjectResponseDTO.builder()
            .title(project.getTitle())
            .description(project.getDescription())
            .owner(mapToUserResponseDTO(project.getOwner()))
            .createdAt(project.getCreatedAt())
            .build();
    }

    public ProjectDetailsDTO getProjectDetails(Long id, SZP_User user) {

        if(projectRepository.findById(id).isPresent()) {
            
            Project project = projectRepository.findById(id).get();
            
            if(project.getMembers().contains(user) || project.getOwner().getId().equals(user.getId())) {
                return ProjectDetailsDTO.builder()
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .createdAt(project.getCreatedAt())
                    .members(project.getMembers())
                    .tasks(project.getTasks())
                    .owner(project.getOwner())
                    .build();
            }
        }

        return null;
    }


    private UserResponseDTO mapToUserResponseDTO(SZP_User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .build();
    }
}
