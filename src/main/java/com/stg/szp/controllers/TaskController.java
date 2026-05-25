package com.stg.szp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.CreateTaskDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.TaskService;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<?> getAllProjectTasks(@PathVariable Long projectId, @AuthenticationPrincipal SZP_User user) {
        List<TaskDetailsDTO> response = taskService.getAllTasksOfProject(user, projectId);

        if(response == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{projectId}")
    public ResponseEntity<?> createTask(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @RequestBody CreateTaskDTO createTaskDTO) {
        TaskDetailsDTO response = taskService.createTask(user, projectId, createTaskDTO);

        if(response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @PutMapping("/{projectId}/{taskId}")
    public ResponseEntity<?> updateTask(@AuthenticationPrincipal SZP_User user,
        @PathVariable Long projectId, @PathVariable Long taskId, @RequestBody CreateTaskDTO createTaskDTO
    ) {
        
        TaskDetailsDTO response = taskService.updateTask(user, projectId, taskId, createTaskDTO);

        if(response == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{projectId}/{taskId}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId, @PathVariable Long taskId) {
        if(taskService.deleteTask(user, projectId, taskId)) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
