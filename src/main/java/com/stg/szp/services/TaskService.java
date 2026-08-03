package com.stg.szp.services;

import com.stg.szp.repos.SZP_UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.stg.szp.DTO.CreateTaskDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.UpcomingTasksDTO;
import com.stg.szp.DTO.UpdateTaskStatusDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Task;
import com.stg.szp.models.TaskPriority;
import com.stg.szp.models.TaskStatus;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.TaskRepository;

import jakarta.transaction.Transactional;

@Service
public class TaskService {
    
    private final SZP_UserRepository SZP_UserRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, SZP_UserRepository SZP_UserRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.SZP_UserRepository = SZP_UserRepository;
    }


    // public List<TaskDetailsDTO> getAllTasksOfProject(SZP_User user, Long projectId) {
        
    //     if(!projectRepository.existsById(projectId)) return null;

    //     Project project = projectRepository.findById(projectId).get();

    //     if(!project.getMembers().contains(user) && !project.getOwner().getId().equals(user.getId())) return null;

    //     return project.getTasks().stream().map((task) -> TaskDetailsDTO.builder()
    //         .id(task.getId())
    //         .title(task.getTitle())
    //         .assigneeEmail(task.getAssignee().getEmail())
    //         .status(task.getStatus())
    //         .createdAt(task.getCreatedAt())
    //         .updatedAt(task.getUpdatedAt())
    //         .description(task.getDescription())
    //         .priority(task.getPriority())
    //         .build()
    //     ).toList();
    // }

    public TaskDetailsDTO createTask(SZP_User user, Long projectId, CreateTaskDTO createTaskDTO) {
        if(!projectRepository.existsById(projectId)) return null;

        Project project = projectRepository.findById(projectId).get();
        
        if(!project.getOwner().getId().equals(user.getId())) return null;

        Integer taskSequence = createTaskDTO.getTaskSequence();
        if (taskSequence == null) {
            Integer maxSequence = taskRepository.findMaxTaskSequenceByProjectId(projectId);
            taskSequence = (maxSequence == null ? 0 : maxSequence) + 1;
        }

        if (taskRepository.existsByProjectIdAndTaskSequence(projectId, taskSequence)) {
            throw new IllegalArgumentException("Task sequence must be unique within the project");
        }

        Task task = new Task();
        if(!createTaskDTO.getAssigneeEmail().isEmpty() || createTaskDTO.getAssigneeEmail() != null) {
            task.setAssignee(SZP_UserRepository.findByEmail(createTaskDTO.getAssigneeEmail()).get());
        } else {
            task.setAssignee(null);
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setProject(project);
        task.setTitle(createTaskDTO.getTitle());
        task.setDescription(createTaskDTO.getDescription());
        task.setStatus(createTaskDTO.getStatus());
        task.setDeadlineAt(createTaskDTO.getDeadlineAt());
        task.setPriority(createTaskDTO.getPriority());
        task.setTaskSequence(taskSequence);

        try {
            task = taskRepository.save(task);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Task sequence must be unique within the project");
        }

        return TaskDetailsDTO.builder()

            .id(task.getId())
            .assigneeEmail(createTaskDTO.getAssigneeEmail())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .deadlineAt(task.getDeadlineAt())
            .build();

    }

    public TaskDetailsDTO updateTask(SZP_User user, Long projectID, Long taskId, CreateTaskDTO createTaskDTO) {
        if(!projectRepository.existsById(projectID) || !taskRepository.existsById(taskId)) return null;

        Project project = projectRepository.findById(projectID).get();
        if(!project.getOwner().getId().equals(user.getId())) return null;

        if(!SZP_UserRepository.existsByEmail(createTaskDTO.getAssigneeEmail())) return null;
        SZP_User userAssignee = SZP_UserRepository.findByEmail(createTaskDTO.getAssigneeEmail()).get();

        if (createTaskDTO.getTaskSequence() != null) {
            if (taskRepository.existsByProjectIdAndTaskSequence(projectID, createTaskDTO.getTaskSequence())
                    && !taskRepository.findById(taskId).get().getTaskSequence().equals(createTaskDTO.getTaskSequence())) {
                throw new IllegalArgumentException("Task sequence must be unique within the project");
            }
        }

        Task task = taskRepository.findById(taskId).get();
        task.setTitle(createTaskDTO.getTitle());
        task.setDescription(createTaskDTO.getDescription());
        task.setAssignee(userAssignee);
        task.setPriority(createTaskDTO.getPriority());
        task.setStatus(createTaskDTO.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        if (createTaskDTO.getTaskSequence() != null) {
            task.setTaskSequence(createTaskDTO.getTaskSequence());
        }
        
        if(task.getStatus().equals(TaskStatus.DONE)) task.setCompletedAt(LocalDateTime.now());
        try {
            taskRepository.save(task);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Task sequence must be unique within the project");
        }

        return TaskDetailsDTO.builder()
            .id(task.getId())
            .assigneeEmail(createTaskDTO.getAssigneeEmail())
            .createdAt(task.getCreatedAt())
            .updatedAt(task.getUpdatedAt())
            .title(task.getTitle())
            .description(task.getDescription())
            .status(task.getStatus())
            .priority(task.getPriority())
            .build();

    }

    public TaskDetailsDTO updateTaskStatus(Long taskId, UpdateTaskStatusDTO statusDto) {
        
        if(taskRepository.existsById(taskId)) {
            Task taskToUpdate = taskRepository.findById(taskId).get();
            taskToUpdate.setStatus(statusDto.getStatus());
            taskRepository.save(taskToUpdate);

            return TaskDetailsDTO.builder()
                .id(taskToUpdate.getId())
                .assigneeEmail(taskToUpdate.getAssignee().getEmail())
                .createdAt(taskToUpdate.getCreatedAt())
                .updatedAt(taskToUpdate.getUpdatedAt())
                .title(taskToUpdate.getTitle())
                .description(taskToUpdate.getDescription())
                .status(taskToUpdate.getStatus())
                .priority(taskToUpdate.getPriority())
                .build();
        }

        return null;
    }

    public boolean deleteTask(SZP_User user, Long projectId, Long taskId) {
        if(!projectRepository.existsById(projectId) || !taskRepository.existsById(taskId)) return false;

        if(!projectRepository.findById(projectId).get().getOwner().getId().equals(user.getId())) return false;

        taskRepository.delete(taskRepository.findById(taskId).get());

        return true;
    }

    public List<TaskDetailsDTO> getUserTasks(SZP_User user) {
        List<Task> tasks = taskRepository.findAllByAssigneeId(user.getId());

        return tasks.stream().map(
            task -> TaskDetailsDTO.builder()
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .deadlineAt(task.getDeadlineAt())
                .assigneeEmail(task.getAssignee().getEmail())
                .title(task.getTitle())
                .projectId(task.getProject().getId())
                .projectTitle(task.getProject().getTitle())
                .status(task.getStatus())
                .priority(task.getPriority())
                .id(task.getId())
                .description(task.getDescription())
                .build()
        ).toList();
    }

    public Long getCountAllUserTasks(Long userId) {
        return taskRepository.countByAssigneeId(userId);
    }

    public TaskStatusCountDTO getCountAllUserTasksByStatuses(Long userId) {
        long todo = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.TODO);
        long inProgress = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.IN_PROGRESS);
        long done = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.DONE);
        long overdue = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.OVERDUE);
        long review = taskRepository.countByAssigneeIdAndStatus(userId, TaskStatus.REVIEW);

        return new TaskStatusCountDTO(todo, inProgress, done, review, overdue);
    }

    public List<UpcomingTasksDTO> getTasksOrderedByDeadline(SZP_User user) {
        
        return taskRepository.findTop5ByAssigneeIdOrderByDeadlineAtAsc(user.getId()).stream().map(
            task -> new UpcomingTasksDTO(
            task.getId(),
            task.getTitle(),
            task.getProject().getTitle(),
            task.getDeadlineAt(),
            task.getStatus())
        ).toList();
        
    }

    @Transactional
    private void refreshOverdueStatuses() {
        LocalDateTime currentDate = LocalDateTime.now();

        List<Task> overdueTasks = taskRepository.findAllByDeadlineAtBeforeAndStatusNot(currentDate, TaskStatus.DONE);

        for(Task task : overdueTasks) {
            if(task.getStatus() != TaskStatus.DONE && task.getStatus() != TaskStatus.OVERDUE) {
                task.setStatus(TaskStatus.OVERDUE);
                task.setUpdatedAt(currentDate);
                taskRepository.save(task);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void updateOverdueTasks() {
        refreshOverdueStatuses();
    }

}
