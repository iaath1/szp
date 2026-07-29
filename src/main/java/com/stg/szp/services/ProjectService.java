package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stg.szp.DTO.EditProjectDTO;
import com.stg.szp.DTO.MyProjectDTO;
import com.stg.szp.DTO.MyProjectsStatsDTO;
import com.stg.szp.DTO.ProjectDetailsDTO;
import com.stg.szp.DTO.ProjectResponseDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.UpcomingTasksDTO;
import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.ProjectStatus;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.TaskStatus;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;
import com.stg.szp.repos.TaskRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SZP_UserRepository userRepository;
    private final TaskRepository taskRepo;

    public ProjectService(ProjectRepository projectRepository, SZP_UserRepository userRepository, TaskRepository taskRepo) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepo = taskRepo;
    }

    @Transactional(readOnly = true)
    public List<MyProjectDTO> getUserProjects(SZP_User user) {
        return projectRepository.findAllByOwnerOrMember(user.getId()).stream()
                .map(project -> MyProjectDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .proggress(getProjectProgress(project.getId()))
                .deadLineAt(project.getDeadlineAt())
                .updatedAt(project.getUpdatedAt())
                .startAt(project.getStartAt())
                .members(getProjectMembersWithLimit(project, 3L))
                .membersCount(project.getMembers().size())
                .build())
                .toList();
    }

    public MyProjectsStatsDTO getUserProjectStats(SZP_User user) {
        Long userId = user.getId();
        Long total = getTotalProjectsCountByUserId(userId);
        Long completed = getTotalStatsByUserIdAndStatus(userId, ProjectStatus.COMPLETED);
        Long inProggress = getTotalStatsByUserIdAndStatus(userId, ProjectStatus.IN_PROGRESS);
        Long onHold = getTotalStatsByUserIdAndStatus(userId, ProjectStatus.ON_HOLD);

        return MyProjectsStatsDTO.builder()
                .total(total)
                .completed(completed)
                .inProggres(inProggress)
                .onHold(onHold)
                .build();
    }

    public ProjectResponseDTO createProject(
            SZP_User user,
            String title,
            String description,
            boolean isPrivate,
            LocalDateTime deadlineAt,
            LocalDateTime startAt,
            ProjectStatus projectStatus
    ) {
        Project project = new Project();
        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(user);
        project.setDescription(description);
        project.setPrivate(isPrivate);
        project.setDeadlineAt(deadlineAt);
        project.setStartAt(startAt);
        project.setTitle(title);
        project.setStatus(projectStatus);

        projectRepository.save(project);

        return ProjectResponseDTO.builder()
                .createdAt(project.getCreatedAt())
                .description(description)
                .title(title)
                .owner(mapToUserResponseDTO(user))
                .status(project.getStatus())
                .isPrivate(project.isPrivate())
                .deadlineAt(deadlineAt)
                .startAt(startAt)
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
                .status(project.getStatus())
                .startAt(project.getStartAt())
                .deadlineAt(project.getDeadlineAt())
                .build();
    }

    @Transactional(readOnly = true)
    public ProjectDetailsDTO getProjectDetails(Long id, SZP_User user) {

        if (projectRepository.findById(id).isPresent()) {

            Project project = projectRepository.findById(id).get();

            if (project.getMembers().contains(user) || project.getOwner().getId().equals(user.getId())) {

                return ProjectDetailsDTO.builder()
                        .id(project.getId())
                        .title(project.getTitle())
                        .description(project.getDescription())
                        .createdAt(project.getCreatedAt())
                        .owner(mapToUserResponseDTO(project.getOwner()))
                        .proggress(getProjectProgress(id))
                        .membersCount(project.getMembers().size())
                        .tasksCount(project.getTasks().size())
                        .status(project.getStatus())
                        .isPrivate(project.isPrivate())
                        .deadlineAt(project.getDeadlineAt())
                        .startAt(project.getStartAt())
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

        if (userRepository.existsByEmail(userEmail)) {
            userToAdd = userRepository.findByEmail(userEmail).get();
        } else {
            return false;
        }

        if (!project.getOwner().getId().equals(userToAdd.getId())) {
            project.getMembers().add(userToAdd);
            projectRepository.save(project);
            return true;
        }

        return false;
    }

    public Long getUserProjectsCount(Long userId) {
        return projectRepository.countByMembers_Id(userId);
    }

    public List<UserResponseDTO> getProjectMembers(Long projectId) {
        Project project = projectRepository.findById(projectId).get();

        return Stream.concat(
                Stream.of(project.getOwner()), // Первый стрим (только из владельца)
                project.getMembers().stream() // Второй стрим (из участников)
        )
                .distinct() // Убираем дубликаты (например, если owner есть в members)
                .map(this::mapToUserResponseDTO) // Конвертируем в DTO
                .toList();                         // Собираем в список
    }

    public TaskStatusCountDTO getProjectTasksStats(Long projectId) {
        Long todo = taskRepo.countByProjectIdAndStatus(projectId, TaskStatus.TODO);
        Long inProgress = taskRepo.countByProjectIdAndStatus(projectId, TaskStatus.IN_PROGRESS);
        Long review = taskRepo.countByProjectIdAndStatus(projectId, TaskStatus.REVIEW);
        Long done = taskRepo.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        Long overdue = taskRepo.countByProjectIdAndStatus(projectId, TaskStatus.OVERDUE);

        return TaskStatusCountDTO.builder()
                .todo(todo)
                .inProgress(inProgress)
                .review(review)
                .done(done)
                .overdue(overdue)
                .build();
    }

    public List<UpcomingTasksDTO> getProjectUpcomingTasks(Long projectId) {
        return taskRepo.findTop5ByProjectIdOrderByDeadlineAtAsc(projectId).stream().map(
            task -> UpcomingTasksDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .deadline(task.getDeadlineAt())
                .status(task.getStatus())
                .build()
        ).toList();
    }

    public List<TaskDetailsDTO> getProjectTasks(Long projectId) {
        return taskRepo.findAllByProjectId(projectId).stream().map(
            task -> TaskDetailsDTO.builder()
                .id(task.getId())
                .assigneeEmail(task.getAssignee() != null ? task.getAssignee().getEmail() : null)
                .createdAt(task.getCreatedAt())
                .deadlineAt(task.getDeadlineAt())
                .priority(task.getPriority())
                .description(task.getDescription())
                .title(task.getTitle())
                .status(task.getStatus())
                .updatedAt(task.getUpdatedAt())
                .build()
        ).toList();

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
                .avatarUrl(user.getAvatarPath())
                .build();
    }

    private Long getProjectProgress(Long projectId) {
        Long completedTasksCount = taskRepo.countByProjectIdAndStatus(projectId, TaskStatus.DONE);
        Long allTasksCount = taskRepo.countByProjectId(projectId);

        if (completedTasksCount == 0) {
            return 0l;
        }

        return (long) (((double) completedTasksCount / allTasksCount) * 100);
    }

    private Long getTotalStatsByUserIdAndStatus(Long userId, ProjectStatus status) {
        Long ownerCount = projectRepository.countByOwnerIdAndStatus(userId, status);
        Long memberCount = projectRepository.countByMembers_IdAndStatus(userId, status);

        return ownerCount + memberCount;
    }

    private Long getTotalProjectsCountByUserId(Long userId) {
        Long ownerCount = projectRepository.countByOwnerId(userId);
        Long memberCount = projectRepository.countByMembers_Id(userId);

        return ((Long) (ownerCount + memberCount));
    }

    private List<UserResponseDTO> getProjectMembersWithLimit(Project project, Long limit) {
        return project.getMembers().stream().map((SZP_User user) -> {
            return mapToUserResponseDTO(user);
        }).limit(limit).toList();
    }
}
