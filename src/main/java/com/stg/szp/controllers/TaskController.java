package com.stg.szp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.CreateTaskDTO;
import com.stg.szp.DTO.NumberResponseDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.UpcomingTasksDTO;
import com.stg.szp.DTO.UpdateTaskStatusDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.TaskStatus;
import com.stg.szp.services.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // @GetMapping("/{projectId}")
    // public ResponseEntity<?> getAllProjectTasks(@PathVariable Long projectId, @AuthenticationPrincipal SZP_User user) {
    //     List<TaskDetailsDTO> response = taskService.getAllTasksOfProject(user, projectId);

    //     if(response == null) {
    //         return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    //     }

    //     return new ResponseEntity<>(response, HttpStatus.OK);
    // }

    @GetMapping("/my")
    public ResponseEntity<List<TaskDetailsDTO>> getMyTasks(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatusCode.valueOf(401));

        List<TaskDetailsDTO> response = taskService.getUserTasks(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER')")
    
    public ResponseEntity<?> createTask(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @RequestBody CreateTaskDTO createTaskDTO) {
        try {
            TaskDetailsDTO response = taskService.createTask(user, projectId, createTaskDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PutMapping("/{projectId}/{taskId}")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER', 'DESIGNER', 'QA_ENGINEER')")

    public ResponseEntity<?> updateTask(@AuthenticationPrincipal SZP_User user,
        @PathVariable Long projectId, @PathVariable Long taskId, @RequestBody CreateTaskDTO createTaskDTO
    ) {
        try {
            TaskDetailsDTO response = taskService.updateTask(user, projectId, taskId, createTaskDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER', 'DESIGNER', 'QA_ENGINEER')")

    public ResponseEntity<TaskDetailsDTO> updateTaskSttaus(@PathVariable Long taskId, @RequestBody UpdateTaskStatusDTO statusDto, @AuthenticationPrincipal SZP_User user) {
        TaskDetailsDTO response = taskService.updateTaskStatus(taskId, statusDto);

        if(response == null) return new ResponseEntity<>(HttpStatusCode.valueOf(404));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}/{taskId}")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER')")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @PathVariable Long taskId) {
        if(taskService.deleteTask(user, projectId, taskId)) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/count")
    public ResponseEntity<NumberResponseDTO> getCountAllUserTasks(@AuthenticationPrincipal SZP_User user) {
        NumberResponseDTO response = new NumberResponseDTO(taskService.getCountAllUserTasks(user.getId()));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/count-by-statuses")
    public ResponseEntity<TaskStatusCountDTO> getCountAllUserTasksByStatuses(@AuthenticationPrincipal SZP_User user) {
        TaskStatusCountDTO response = taskService.getCountAllUserTasksByStatuses(user.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcomingTasksDTO>> getUpcomingTasks(@AuthenticationPrincipal SZP_User user) {
        List<UpcomingTasksDTO> tasks = taskService.getTasksOrderedByDeadline(user);

        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }
}
