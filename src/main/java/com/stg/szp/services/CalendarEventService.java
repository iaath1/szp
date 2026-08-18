package com.stg.szp.services;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cglib.core.Local;

import org.springframework.stereotype.Service;

import com.stg.szp.DTO.CalendarResponseDTO;
import com.stg.szp.models.EventType;
import com.stg.szp.models.Milestone;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Task;
import com.stg.szp.repos.CalendarEventRepository;
import com.stg.szp.repos.MilestoneRepository;
import com.stg.szp.repos.ProjectRepository;
import com.stg.szp.repos.TaskRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CalendarEventService {
    private final CalendarEventRepository calendarRepo;
    private final TaskRepository taskRepo;
    private final ProjectRepository projectRepo;
    private final MilestoneRepository milestoneRepo;

    public List<CalendarResponseDTO> getMonthlyEvents(int year, int month, SZP_User user) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return calendarRepo.findEventsForUserInMonth(user.getId(), startOfMonth, endOfMonth).stream().map(
            event -> CalendarResponseDTO.builder()
                .id(event.getId())
                .description(event.getDescription())
                .title(event.getTitle())
                .displayType(event.getType())
                .endTime(event.getEndTime())
                .starTime(event.getStartTime())
                .sourceType("EVENT")
                .projectName(event.getProject() != null ? event.getProject().getTitle() : null)
                .build()
        ).toList();
    }

    public List<CalendarResponseDTO> getUserEvents(Long userId, LocalDateTime starTime, LocalDateTime endTime) {
        List<Task> userTasks = taskRepo.findByAssigneeIdAndDeadlineAtBetween(userId, starTime, endTime);

        return userTasks.stream().map(
            task -> CalendarResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .starTime(task.getCreatedAt())
                .endTime(task.getDeadlineAt())
                .sourceType("TASK")
                .displayType(EventType.DEADLINE)
                .projectName(task.getProject().getTitle())
                .build()
        ).toList();
    }

    public List<CalendarResponseDTO> getMilestoneEvents(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Project> projects = projectRepo.findAllByOwnerOrMember(userId);
        List<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());

        if(projectIds != null && !projectIds.isEmpty()) {
            return milestoneRepo.findByProjectIdInAndDueDateBetween(projectIds, startTime, endTime).stream().map(
                ms -> CalendarResponseDTO.builder()
                    .id(ms.getId())
                    .title(ms.getTitle())
                    .description("Milestone in project: " + ms.getProject().getTitle())
                    .starTime(ms.getDueDate())
                    .endTime(ms.getDueDate())
                    .sourceType("MILESTONE")
                    .displayType(EventType.MILESTONE)
                    .projectName(ms.getProject().getTitle())
                    .build()
            ).toList();
        }

        return null;
    }

    public List<CalendarResponseDTO> getAllUserEvents(int year, int month, SZP_User user) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<CalendarResponseDTO> monthlyEvents = new ArrayList<>(getMonthlyEvents(year, month, user));
        List<CalendarResponseDTO> userEvents = getUserEvents(user.getId(), startOfMonth, endOfMonth);
        List<CalendarResponseDTO> milestoneEvents = getMilestoneEvents(user.getId(), startOfMonth, endOfMonth);

        monthlyEvents.addAll(userEvents);

        if(milestoneEvents != null) monthlyEvents.addAll(milestoneEvents);

        return monthlyEvents;
    }
}
