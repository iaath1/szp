package com.stg.szp.services;

import com.stg.szp.repos.SZP_UserRepository;
import com.stg.szp.repos.SubtaskRepository;
import com.stg.szp.repos.TaskCommentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.stg.szp.DTO.CreateTaskDTO;
import com.stg.szp.DTO.ProjectFileDTO;
import com.stg.szp.DTO.SubtaskDTO;
import com.stg.szp.DTO.TaskCommentDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.UpcomingTasksDTO;
import com.stg.szp.DTO.UpdateTaskStatusDTO;
import com.stg.szp.models.NotificationType;
import com.stg.szp.models.Project;
import com.stg.szp.models.ProjectFile;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Subtask;
import com.stg.szp.models.Task;
import com.stg.szp.models.TaskComment;
import com.stg.szp.models.TaskStatus;
import com.stg.szp.repos.ProjectFileRepository;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.TaskRepository;

import jakarta.transaction.Transactional;

@Service
public class TaskService {
    
    private final SZP_UserRepository SZP_UserRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SubtaskRepository subtaskRepo;
    private final TaskCommentRepository taskCommentRepo;
    private final ProjectFileRepository projectFileRepo;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository,
        ProjectRepository projectRepository,
        SZP_UserRepository SZP_UserRepository,
        SubtaskRepository subtaskRepo,
        TaskCommentRepository taskCommentRepo,
        ProjectFileRepository projectFileRepo,
        NotificationService notificationService
        ) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.SZP_UserRepository = SZP_UserRepository;
        this.subtaskRepo = subtaskRepo;
        this.taskCommentRepo = taskCommentRepo;
        this.projectFileRepo = projectFileRepo;
        this.notificationService = notificationService;
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
        if (createTaskDTO.getAssigneeEmail() != null && !createTaskDTO.getAssigneeEmail().trim().isEmpty()) {
            task.setAssignee(SZP_UserRepository.findByEmail(createTaskDTO.getAssigneeEmail()).orElse(null));
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

        if(task.getAssignee() != null) {
            notificationService.createNotification(task.getAssignee(),
            NotificationType.TASK_UPDATE, 
            "New task", 
            "New task was assigned to you: " + task.getTitle(), 
            "/projects/" + task.getProject().getId());
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
            .attachments(mapAttachmentsToListDto(task.getAttachments()))
            .subtasks(mapTaskSubtasksToDto(task))
            .build();

    }

    public TaskDetailsDTO updateTask(SZP_User user, Long projectID, Long taskId, CreateTaskDTO createTaskDTO) {
        if(!projectRepository.existsById(projectID) || !taskRepository.existsById(taskId)) return null;

        Project project = projectRepository.findById(projectID).get();
        if(!project.getOwner().getId().equals(user.getId())) return null;

        SZP_User userAssignee = null;
        if (createTaskDTO.getAssigneeEmail() != null && !createTaskDTO.getAssigneeEmail().trim().isEmpty()) {
            if(!SZP_UserRepository.existsByEmail(createTaskDTO.getAssigneeEmail())) return null;
            userAssignee = SZP_UserRepository.findByEmail(createTaskDTO.getAssigneeEmail()).get();
        }

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

        if(task.getAssignee() != null) {
            notificationService.createNotification(userAssignee, NotificationType.TASK_UPDATE,
            task.getTitle(),
            "Task updated: " + task.getTitle(),
            "/projects/" + task.getProject().getId());
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
            .attachments(mapAttachmentsToListDto(task.getAttachments()))
            .subtasks(mapTaskSubtasksToDto(task))
            .build();

    }

    public TaskDetailsDTO updateTaskStatus(Long taskId, UpdateTaskStatusDTO statusDto) {
        
        if(taskRepository.existsById(taskId)) {
            Task taskToUpdate = taskRepository.findById(taskId).get();
            taskToUpdate.setStatus(statusDto.getStatus());

            if(taskToUpdate.getStatus().equals(TaskStatus.DONE)) taskToUpdate.setCompletedAt(LocalDateTime.now());
            taskRepository.save(taskToUpdate);

            if(taskToUpdate.getAssignee() != null) {
                notificationService.createNotification(taskToUpdate.getAssignee(), NotificationType.TASK_UPDATE,
                taskToUpdate.getTitle(),
                "Task status updated: " + taskToUpdate.getTitle() + taskToUpdate.getStatus(),
                "/projects/" + taskToUpdate.getProject().getId());
            }

            return TaskDetailsDTO.builder()
                .id(taskToUpdate.getId())
                .assigneeEmail(taskToUpdate.getAssignee().getEmail())
                .createdAt(taskToUpdate.getCreatedAt())
                .updatedAt(taskToUpdate.getUpdatedAt())
                .title(taskToUpdate.getTitle())
                .description(taskToUpdate.getDescription())
                .status(taskToUpdate.getStatus())
                .priority(taskToUpdate.getPriority())
                .attachments(mapAttachmentsToListDto(taskToUpdate.getAttachments()))
                .subtasks(mapTaskSubtasksToDto(taskToUpdate))
                .build();
        }

        return null;
    }

    @Transactional
    public TaskDetailsDTO addAttachment(Long taskId, Long fileId) {
        if(taskRepository.existsById(taskId) && projectFileRepo.existsById(fileId)) {
            Task taskToUpdate = taskRepository.findById(taskId).get();
            ProjectFile file = projectFileRepo.findById(fileId).get();

            file.setTask(taskToUpdate);
            projectFileRepo.save(file);
            
            // refresh task attachments list if necessary
            taskToUpdate.getAttachments().add(file);

            return TaskDetailsDTO.builder()
                .id(taskToUpdate.getId())
                .assigneeEmail(taskToUpdate.getAssignee() != null ? taskToUpdate.getAssignee().getEmail() : null)
                .createdAt(taskToUpdate.getCreatedAt())
                .updatedAt(taskToUpdate.getUpdatedAt())
                .title(taskToUpdate.getTitle())
                .description(taskToUpdate.getDescription())
                .status(taskToUpdate.getStatus())
                .priority(taskToUpdate.getPriority())
                .attachments(mapAttachmentsToListDto(taskToUpdate.getAttachments()))
                .subtasks(mapTaskSubtasksToDto(taskToUpdate))
                .build();
        }
        return null;
    }

    @Transactional
    public TaskDetailsDTO removeAttachment(Long taskId, Long fileId) {
        if(taskRepository.existsById(taskId) && projectFileRepo.existsById(fileId)) {
            Task taskToUpdate = taskRepository.findById(taskId).get();
            ProjectFile file = projectFileRepo.findById(fileId).get();

            file.setTask(null);
            projectFileRepo.save(file);
            
            taskToUpdate.getAttachments().remove(file);

            return TaskDetailsDTO.builder()
                .id(taskToUpdate.getId())
                .assigneeEmail(taskToUpdate.getAssignee() != null ? taskToUpdate.getAssignee().getEmail() : null)
                .createdAt(taskToUpdate.getCreatedAt())
                .updatedAt(taskToUpdate.getUpdatedAt())
                .title(taskToUpdate.getTitle())
                .description(taskToUpdate.getDescription())
                .status(taskToUpdate.getStatus())
                .priority(taskToUpdate.getPriority())
                .attachments(mapAttachmentsToListDto(taskToUpdate.getAttachments()))
                .subtasks(mapTaskSubtasksToDto(taskToUpdate))
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
                .attachments(mapAttachmentsToListDto(task.getAttachments()))
                .subtasks(mapTaskSubtasksToDto(task))
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

    public List<TaskDetailsDTO> getUserTasksByStatus(SZP_User user, TaskStatus status) {
        return taskRepository.findAllByAssigneeIdAndStatus(user.getId(), status).stream().map(
            task -> TaskDetailsDTO.builder()
                .id(task.getId())
                .assigneeEmail(task.getAssignee().getEmail())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .deadlineAt(task.getDeadlineAt())
                .title(task.getTitle())
                .projectId(task.getProject().getId())
                .projectTitle(task.getProject().getTitle())
                .priority(task.getPriority())
                .status(task.getStatus())
                .description(task.getDescription())
                .attachments(mapAttachmentsToListDto(task.getAttachments()))
                .commentsCount(task.getComments().size())
                .subtasks(mapTaskSubtasksToDto(task))
                .build()
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

    private List<SubtaskDTO> mapTaskSubtasksToDto(Task task) {
        if (task.getSubtasks() == null) return new ArrayList<>();

        return task.getSubtasks().stream().map(
            subtask -> SubtaskDTO.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .isCompleted(subtask.isCompleted())
                .build()
        ).toList();
    }

    private SubtaskDTO mapSubtaskToDTO(Subtask subtask) {
        return SubtaskDTO.builder()
            .id(subtask.getId())
            .title(subtask.getTitle())
            .isCompleted(subtask.isCompleted())
            .build();
    }

    public SubtaskDTO createSubtask(Long taskId, SubtaskDTO dto) {
        if(taskRepository.findById(taskId).isEmpty()) return null;

        Task task = taskRepository.findById(taskId).get();
        Subtask subtask = new Subtask();
        subtask.setTitle(dto.getTitle());
        subtask.setCompleted(false);
        subtask.setTask(task);

        Subtask saved = subtaskRepo.save(subtask);

        return mapSubtaskToDTO(saved);
    }

    public SubtaskDTO updateSubtask(Long taskId, Long subtaskId, SubtaskDTO dto) {
        if(!taskRepository.existsById(taskId) || !subtaskRepo.existsById(subtaskId)) return null;

        Subtask subtask = subtaskRepo.findById(subtaskId).get();
        subtask.setTitle(dto.getTitle());
        subtask.setCompleted(dto.isCompleted());
        
        return mapSubtaskToDTO(subtaskRepo.save(subtask));
    }

    // need to be added verification is subtask assigned to task. It need to be added to all similar methods
    @Transactional
    public boolean deleteSubtask(Long taskId, Long subtaskId) {
        if (taskRepository.existsById(taskId) && subtaskRepo.existsById(subtaskId)) {
            Task task = taskRepository.findById(taskId).get();
            Subtask subtask = subtaskRepo.findById(subtaskId).get();
            
            // Ensure the subtask belongs to this task
            if (subtask.getTask().getId().equals(taskId)) {
                task.getSubtasks().remove(subtask);
                taskRepository.save(task);
                // Due to orphanRemoval=true, removing it from the list and saving the task 
                // will automatically delete it from the database.
                return true;
            }
        }

        return false;
    }

    public TaskCommentDTO addCommentToTask(Long taskId, TaskCommentDTO dto, SZP_User author) {
        if(!taskRepository.existsById(taskId) || author == null) return null;

        TaskComment comment = new TaskComment();
        comment.setAuthor(author);
        comment.setContent(dto.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setTask(taskRepository.findById(taskId).get());

        return mapTaskCommentToDto(taskCommentRepo.save(comment));

    }

    public List<TaskCommentDTO> getTaskComments(Long taskId) {
        if(!taskRepository.existsById(taskId)) return null;

        return taskCommentRepo.findAllByTaskIdOrderByCreatedAtAsc(taskId).stream().map(
            comment -> mapTaskCommentToDto(comment)
        ).toList();
    }

    public boolean deleteTaskComment(Long commentId, SZP_User user) {
        if(!taskCommentRepo.existsById(commentId)) return false;

        TaskComment comment = taskCommentRepo.findById(commentId).get();
        if(!comment.getAuthor().equals(user)) return false;

        taskCommentRepo.deleteById(commentId);
        return true;
    }

    private TaskCommentDTO mapTaskCommentToDto(TaskComment comment) {
        return TaskCommentDTO.builder()
            .id(comment.getId())
            .authorAvatarUrl(comment.getAuthor().getAvatarPath())
            .authorEmail(comment.getAuthor().getEmail())
            .authorName(comment.getAuthor().getName() + " " + comment.getAuthor().getSurname())
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .build();
    }

    private List<ProjectFileDTO> mapAttachmentsToListDto(List<ProjectFile> files) {
        return files.stream().map(
            file -> ProjectFileDTO.builder()
                .id(file.getId())
                .fileUrl(file.getStoredName())
                .name(file.getOriginalName())
                .type(file.getContentType())
                .size(file.getSize())
                .taskTitle(file.getTask() == null ? null : file.getTask().getTitle())    
                .taskId(file.getTask() == null ? null : file.getTask().getId())
                .uploadDate(file.getUploadetAt())
                .uploaderName(file.getUploader().getName() + " " + file.getUploader().getSurname())
                .build()
        ).toList();
    }

    public List<Map<String, Object>> getTaskVelocityChart(Long userId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<Object[]> results = taskRepository.countCompletedTasksByDate(userId, startDate.atStartOfDay());

        Map<String, Long> res = new HashMap<>();
        for(Object[] row : results) {
            String dateStr = row[0].toString();
            Long count = ((Number) row[1]).longValue();
            res.put(dateStr, count);
        }

        List<Map<String, Object>> chartData = new ArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for(int i = 0; i < days; i++) {
            LocalDate currenDate = startDate.plusDays(i);
            String dbDataKey = currenDate.toString();

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", currenDate.format(formatter));
            dayData.put("completed", res.getOrDefault(dbDataKey, 0L));

            chartData.add(dayData);
        }

        return chartData;
    }

}
