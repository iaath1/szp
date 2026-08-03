package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stg.szp.DTO.AddUserToProjectDTO;
import com.stg.szp.DTO.EditProjectDTO;
import com.stg.szp.DTO.MyProjectDTO;
import java.util.Optional;
import com.stg.szp.DTO.MyProjectsStatsDTO;
import com.stg.szp.DTO.ProjectDetailsDTO;
import com.stg.szp.DTO.ProjectResponseDTO;
import com.stg.szp.DTO.TaskDetailsDTO;
import com.stg.szp.DTO.TaskStatusCountDTO;
import com.stg.szp.DTO.UpcomingTasksDTO;
import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.Project;
import com.stg.szp.models.ProjectMember;
import com.stg.szp.models.ProjectRole;
import com.stg.szp.models.ProjectStatus;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.TaskStatus;
import com.stg.szp.repos.ProjectMemberRepository;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.SZP_UserRepository;
import com.stg.szp.repos.TaskRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final SZP_UserRepository userRepository;
    private final TaskRepository taskRepo;
    private final ProjectMemberRepository projectMemberRepo;

    public ProjectService(
            ProjectRepository projectRepository,
            SZP_UserRepository userRepository,
            TaskRepository taskRepo,
            ProjectMemberRepository projectMemberRepo
        ) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepo = taskRepo;
        this.projectMemberRepo = projectMemberRepo;
    }

    @Transactional(readOnly = true)
    public List<MyProjectDTO> getUserProjects(SZP_User user) {
        return projectRepository.findAllByOwnerOrMember(user.getId()).stream()
                .map(project -> MyProjectDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .progress(getProjectProgress(project.getId()))
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
            String projectKey,
            String description,
            boolean isPrivate,
            LocalDateTime deadlineAt,
            LocalDateTime startAt,
            ProjectStatus projectStatus
    ) {
        if (projectKey == null || projectKey.isBlank()) {
            throw new IllegalArgumentException("Project key is required");
        }

        String normalizedKey = projectKey.trim().toUpperCase();
        boolean exists = projectRepository.existsByOwnerIdAndProjectKey(user.getId(), normalizedKey);
        if (exists) {
            throw new IllegalArgumentException("Project key must be unique for this user");
        }

        Project project = new Project();
        project.setCreatedAt(LocalDateTime.now());
        project.setOwner(user);
        project.setDescription(description);
        project.setPrivate(isPrivate);
        project.setDeadlineAt(deadlineAt);
        project.setStartAt(startAt);
        project.setTitle(title);
        project.setProjectKey(normalizedKey);
        project.setStatus(projectStatus);

        try {
            projectRepository.save(project);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Project key must be unique for this user");
        }

        projectMemberRepo.save(new ProjectMember(user, project, ProjectRole.PROJECT_MANAGER));

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

        if (editProjectDTO.getProjectKey() != null && !editProjectDTO.getProjectKey().isBlank()) {
            String normalizedKey = editProjectDTO.getProjectKey().trim().toUpperCase();
            Optional<Project> existingProject = projectRepository.findByOwnerIdAndProjectKey(user.getId(), normalizedKey);
            if (existingProject.isPresent() && !existingProject.get().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Project key must be unique for this user");
            }
            project.setProjectKey(normalizedKey);
        }

        project.setTitle(editProjectDTO.getTitle());
        project.setDescription(editProjectDTO.getDescription());
        project.setStatus(editProjectDTO.getStatus());
        project.setStartAt(editProjectDTO.getStartAt());
        project.setDeadlineAt(editProjectDTO.getDeadlineAt());

        try {
            projectRepository.save(project);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Project key must be unique for this user");
        }

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

    public boolean addNewMemberToProject(SZP_User user, Long projectId, AddUserToProjectDTO dto) {
        Project project = null;

        if (projectRepository.existsById(projectId)) {
            project = projectRepository.findById(projectId).get();
        } else {
            return false;
        }

        SZP_User userToAdd = null;

        if (userRepository.existsByEmail(dto.getEmail())) {
            userToAdd = userRepository.findByEmail(dto.getEmail()).get();
        } else {
            return false;
        }

        if (!project.getOwner().getId().equals(userToAdd.getId())) {
            
            project.getMembers().add(userToAdd);
            projectMemberRepo.save(new ProjectMember(userToAdd, project, dto.getRole()));
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

    public List<MyProjectDTO> findUserProjectByStatus(SZP_User user, String status) {
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.toUpperCase());
        if(projectStatus == null) {
            throw new IllegalArgumentException("Project status: " + status + " does not exists.");
        }

        return projectRepository.findAllByOwnerOrMemberAndStatus(user.getId(), projectStatus).stream().map(
            project -> 
            MyProjectDTO.builder()
            .id(project.getId())
            .title(project.getTitle())
            .description(project.getDescription())
            .status(project.getStatus())
            .progress(getProjectProgress(project.getId()))
            .deadLineAt(project.getDeadlineAt())
            .updatedAt(project.getUpdatedAt())
            .startAt(project.getStartAt())
            .members(getProjectMembersWithLimit(project, 3L))
            .membersCount(project.getMembers().size())
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
