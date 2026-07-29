package com.stg.szp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.AddUserToProjectDTO;
import com.stg.szp.DTO.CreateProjectDTO;
import com.stg.szp.DTO.EditProjectDTO;
import com.stg.szp.DTO.MyProjectDTO;
import com.stg.szp.DTO.MyProjectsStatsDTO;
import com.stg.szp.DTO.NumberResponseDTO;
import com.stg.szp.DTO.ProjectDetailsDTO;
import com.stg.szp.DTO.ProjectResponseDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.UpcomingTasksDTO;
import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<MyProjectDTO>> getMyProjects(@AuthenticationPrincipal SZP_User user) {
        List<MyProjectDTO> projects = projectService.getUserProjects(user);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/stats")
    public ResponseEntity<MyProjectsStatsDTO> getMyProjectsStats(@AuthenticationPrincipal SZP_User user) {
        MyProjectsStatsDTO response = projectService.getUserProjectStats(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@AuthenticationPrincipal SZP_User user,
            @RequestBody CreateProjectDTO createProjectDTO) {
        return ResponseEntity
                .ok(
                        projectService.createProject(
                                user, createProjectDTO.getTitle(),
                                createProjectDTO.getDescription(),
                                createProjectDTO.isPrivate(),
                                createProjectDTO.getDeadlineAt(),
                                createProjectDTO.getStartAt(),
                                createProjectDTO.getStatus()
                        ));
    }

    @PatchMapping("/edit/{id}")
    public ResponseEntity<ProjectResponseDTO> editProject(@AuthenticationPrincipal SZP_User user, @PathVariable Long id,
            @RequestBody EditProjectDTO editProjectDTO) {
        try {
            return ResponseEntity.ok(projectService.changeProject(user, editProjectDTO, id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInfoAboutProject(@AuthenticationPrincipal SZP_User user, @PathVariable Long id) {
        ProjectDetailsDTO response = projectService.getProjectDetails(id, user);

        if (response == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

    @GetMapping("tasks-stats/{id}")
    public ResponseEntity<?> getProjectTasksStats(@AuthenticationPrincipal SZP_User user, @PathVariable Long id) {

        if (user != null) {
            TaskStatusCountDTO response = projectService.getProjectTasksStats(id);
            return ResponseEntity.ok(response);
        }

        return new ResponseEntity<>(HttpStatusCode.valueOf(403));

    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<List<UserResponseDTO>> getProjectMembers(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId) {
        if (user != null) {
            List<UserResponseDTO> response = projectService.getProjectMembers(projectId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<?> addNewMemberToProject(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId,
            @RequestBody AddUserToProjectDTO addUserToProjectDTO) {
        boolean isProjectSaved = projectService.addNewMemberToProject(user, projectId, addUserToProjectDTO.getEmail());

        if (isProjectSaved) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/count")
    public ResponseEntity<NumberResponseDTO> getAllProjectsCount(@AuthenticationPrincipal SZP_User user) {
        NumberResponseDTO response = new NumberResponseDTO(projectService.getUserProjectsCount(user.getId()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/upcoming-tasks/{projectId}")
    public ResponseEntity<List<UpcomingTasksDTO>> getProjectUpcomingTasks(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId) {
        if (user != null) {
            List<UpcomingTasksDTO> response = projectService.getProjectUpcomingTasks(projectId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/tasks/{projectId}")
    public ResponseEntity<List<TaskDetailsDTO>> getProjectTasks(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId) {
        if (user != null) {
            List<TaskDetailsDTO> response = projectService.getProjectTasks(projectId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

}
