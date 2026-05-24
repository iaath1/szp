package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stg.szp.DTO.EditProjectDTO;
import com.stg.szp.DTO.ProjectDetailsDTO;
import com.stg.szp.DTO.ProjectResponseDTO;
import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SZP_UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, SZP_UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    public ProjectDetailsDTO getProjectDetails(Long id, SZP_User user) {

        if(projectRepository.findById(id).isPresent()) {
            
            Project project = projectRepository.findById(id).get();
            
            if(project.getMembers().contains(user) || project.getOwner().getId().equals(user.getId())) {

                Set<UserResponseDTO> memberDTOs = project.getMembers().stream()
                    .map(this::mapToUserResponseDTO)
                    .collect(Collectors.toSet());

                return ProjectDetailsDTO.builder()
                    .id(project.getId())
                    .title(project.getTitle())
                    .description(project.getDescription())
                    .createdAt(project.getCreatedAt())
                    .members(memberDTOs)
                    .tasks(project.getTasks())
                    .owner(mapToUserResponseDTO(project.getOwner()))
                    .build();
            }
        }

        return null;
    }


    public boolean addNewMemberToProject(SZP_User user, Long projectId, String userEmail) {
        Project project = null;

        if (projectRepository.existsById(projectId)) {
            project = projectRepository.findById(projectId).get();
        } else {
            return false;
        }

        SZP_User userToAdd = null;

        if(userRepository.existsByEmail(userEmail)) {
            userToAdd = userRepository.findByEmail(userEmail).get();
        } else {
            return false;
        }

        if(project.getOwner().getId().equals(user.getId())) {
            if(!project.getOwner().getId().equals(userToAdd.getId())) {
                project.getMembers().add(userToAdd);
                projectRepository.save(project);
            }
        }

        return false;
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
