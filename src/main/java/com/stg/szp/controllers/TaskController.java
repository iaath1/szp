package com.stg.szp.controllers;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.CreateTaskDTO;
import com.stg.szp.DTO.NumberResponseDTO;
import com.stg.szp.DTO.SubtaskDTO;
import com.stg.szp.DTO.TaskCommentDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.TeamWorkloadDTO;
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
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<TaskDetailsDTO> response = taskService.getUserTasks(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/{projectId}")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER', 'OWNER')")
    
    public ResponseEntity<?> createTask(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @RequestBody CreateTaskDTO createTaskDTO) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        try {
            TaskDetailsDTO response = taskService.createTask(user, projectId, createTaskDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PutMapping("/{projectId}/{taskId}")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER', 'DESIGNER', 'QA_ENGINEER', 'OWNER')")

    public ResponseEntity<?> updateTask(@AuthenticationPrincipal SZP_User user,
        @PathVariable Long projectId, @PathVariable Long taskId, @RequestBody CreateTaskDTO createTaskDTO
    ) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        try {
            TaskDetailsDTO response = taskService.updateTask(user, projectId, taskId, createTaskDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{taskId}/status")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER', 'DESIGNER', 'QA_ENGINEER', 'OWNER')")

    public ResponseEntity<TaskDetailsDTO> updateTaskSttaus(@PathVariable Long taskId, @RequestBody UpdateTaskStatusDTO statusDto, @AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        TaskDetailsDTO response = taskService.updateTaskStatus(taskId, statusDto, user);

        if(response == null) return new ResponseEntity<>(HttpStatusCode.valueOf(404));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{taskId}/attachments/{fileId}")
    public ResponseEntity<TaskDetailsDTO> addAttachment(@AuthenticationPrincipal SZP_User user, @PathVariable Long taskId, @PathVariable Long fileId) {
        TaskDetailsDTO response = taskService.addAttachment(user, taskId, fileId);
        if(response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}/attachments/{fileId}")
    public ResponseEntity<TaskDetailsDTO> removeAttachment(@PathVariable Long taskId, @PathVariable Long fileId) {
        TaskDetailsDTO response = taskService.removeAttachment(taskId, fileId);
        if(response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}/{taskId}")
    @PreAuthorize("@projectSecurity.hasAnyRole(principal, #projectId, 'PROJECT_MANAGER', 'DEVELOPER', 'OWNER')")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @PathVariable Long taskId) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if(taskService.deleteTask(user, projectId, taskId)) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/count")
    public ResponseEntity<NumberResponseDTO> getCountAllUserTasks(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        NumberResponseDTO response = new NumberResponseDTO(taskService.getCountAllUserTasks(user.getId()), 0L);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/count-by-statuses")
    public ResponseEntity<TaskStatusCountDTO> getCountAllUserTasksByStatuses(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        TaskStatusCountDTO response = taskService.getCountAllUserTasksByStatuses(user.getId());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<UpcomingTasksDTO>> getUpcomingTasks(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        List<UpcomingTasksDTO> tasks = taskService.getTasksOrderedByDeadline(user);

        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @GetMapping("/my/{status}")
    public ResponseEntity<List<TaskDetailsDTO>> getTasksByStatus(@AuthenticationPrincipal SZP_User user, @PathVariable TaskStatus status) {
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        List<TaskDetailsDTO> response = taskService.getUserTasksByStatus(user, status);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<SubtaskDTO> createSubtask(@PathVariable Long taskId, @RequestBody SubtaskDTO dto) {
        SubtaskDTO response = taskService.createSubtask(taskId, dto);
        if (response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PatchMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<SubtaskDTO> updateSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId, @RequestBody SubtaskDTO dto) {
        SubtaskDTO response = taskService.updateSubtask(taskId, subtaskId, dto);

        if (response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}/subtasks/{subtaskId}")
    public ResponseEntity<?> deleteSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId) {
        if(taskService.deleteSubtask(taskId, subtaskId)) return new ResponseEntity<>(HttpStatus.OK);

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<TaskCommentDTO>> getTaskComments(@PathVariable Long taskId) {
        List<TaskCommentDTO> response = taskService.getTaskComments(taskId);
        if(response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<TaskCommentDTO> addCommentToTask(
            @PathVariable Long taskId,
            @RequestBody TaskCommentDTO dto,
            @AuthenticationPrincipal SZP_User user
        ) {

        TaskCommentDTO response = taskService.addCommentToTask(taskId, dto, user);
        if(response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{taskId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long taskId, @PathVariable Long commentId, @AuthenticationPrincipal SZP_User user) {
        boolean isDeleted = taskService.deleteTaskComment(commentId, user);
        if(isDeleted) return new ResponseEntity<>(HttpStatus.OK);

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @GetMapping("/velocity")
    public ResponseEntity<List<Map<String, Object>>> getTaskVelocity(@AuthenticationPrincipal SZP_User user, 
        @RequestParam(defaultValue = "30") int days
    ) {

        List<Map<String, Object>> res = taskService.getTaskVelocityChart(user.getId(), days);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/team-workload")
    public ResponseEntity<List<TeamWorkloadDTO>> getTeamWorkload(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        return new ResponseEntity<>(taskService.getTeamWorkload(user), HttpStatus.OK);
    }

    @GetMapping("/project-velocity")
    public ResponseEntity<List<Map<String, Object>>> getProjectVelocityChart(@RequestParam Long projectId, @RequestParam(defaultValue = "30") int days) {
        List<Map<String, Object>> response = taskService.getProjectTaskVelocityChart(projectId, days);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
