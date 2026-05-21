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
import org.springframework.web.service.annotation.PatchExchange;

import com.stg.szp.DTO.CreateProjectDTO;
import com.stg.szp.DTO.EditProjectDTO;
import com.stg.szp.DTO.ProjectDetailsDTO;
import com.stg.szp.DTO.ProjectResponseDTO;
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
    public ResponseEntity<List<ProjectResponseDTO>> getMyProjects(@AuthenticationPrincipal SZP_User user) {
        List<ProjectResponseDTO> projects = projectService.getUserProjects(user);
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(@AuthenticationPrincipal SZP_User user,
            @RequestBody CreateProjectDTO createProjectDTO) {
        return ResponseEntity
                .ok(projectService.createProject(user, createProjectDTO.getTitle(), createProjectDTO.getDescription()));
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

        if(response == null) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else {
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }

}
