package com.stg.szp.services;

import com.stg.szp.DTO.ReportResponseDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.Task;
import com.stg.szp.models.TaskStatus;
import com.stg.szp.repos.TaskRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TaskRepository taskRepo;

    public ReportService(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;
    }

    public ReportResponseDTO generateReport(SZP_User user, String period) {
        List<Task> userTasks = taskRepo.findAllByAssigneeId(user.getId());

        int completed = 0;
        int overdue = 0;

        for (Task task : userTasks) {
            if (task.getStatus() == TaskStatus.DONE) {
                completed++;
            }
            if (task.getDeadlineAt() != null && task.getDeadlineAt().isBefore(LocalDateTime.now()) && task.getStatus() != TaskStatus.DONE) {
                overdue++;
            }
        }

        int score = userTasks.isEmpty() ? 0 : (int) Math.round((double) completed / userTasks.size() * 100);

        ReportResponseDTO.MetricsDTO metrics = ReportResponseDTO.MetricsDTO.builder()
                .completedTasks(completed)
                .overdueTasks(overdue)
                .productivityScore(score)
                .build();

        // Задачи по проектам
        Map<String, Long> tasksPerProject = userTasks.stream()
                .filter(t -> t.getProject() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getProject().getTitle(),
                        Collectors.counting()
                ));

        List<ReportResponseDTO.ProjectDataDTO> projectData = tasksPerProject.entrySet().stream()
                .map(e -> ReportResponseDTO.ProjectDataDTO.builder()
                        .name(e.getKey())
                        .value(e.getValue().intValue())
                        .build())
                .toList();

        Map<DayOfWeek, Long> completedTasksByDay = userTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE && t.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().getDayOfWeek(),
                        Collectors.counting()
                ));

        List<ReportResponseDTO.TimeDataDTO> timeData = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            timeData.add(ReportResponseDTO.TimeDataDTO.builder()
                    .name(day.name().substring(0, 3))
                    .tasks(completedTasksByDay.getOrDefault(day, 0L).intValue())
                    .build());
        }

        return ReportResponseDTO.builder()
                .metrics(metrics)
                .projectData(projectData)
                .timeData(timeData)
                .build();
    }
}

