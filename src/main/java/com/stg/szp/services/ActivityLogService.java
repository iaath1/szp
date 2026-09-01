package com.stg.szp.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.stg.szp.DTO.ActivityLogDTO;
import com.stg.szp.models.ActivityLog;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.ActivityLogRepository;

import lombok.AllArgsConstructor;
import lombok.Data;

@Service
@Data
@AllArgsConstructor
public class ActivityLogService {
    private final ActivityLogRepository activityRepo;

    public void logActivity(String type, String message, Project project, SZP_User user) {
        ActivityLog log = ActivityLog.builder()
            .type(type)
            .message(message)
            .project(project)
            .user(user)
            .build();

        activityRepo.save(log);
    }

    public List<ActivityLogDTO> getRecentActivities(SZP_User user, int limit) {
        List<ActivityLog> logs = activityRepo.findRecentActivitiesForUser(user.getId(), PageRequest.of(0, limit));

        return logs.stream().map(
            log -> ActivityLogDTO.builder()
                .id(log.getId())
                .type(log.getType())
                .message(log.getMessage())
                .projectName(log.getProject().getTitle())
                .projectId(log.getProject().getId())
                .userName(log.getUser() != null ? log.getUser().getName() + " " + log.getUser().getSurname() : "System")
                .userAvatarUrl(log.getUser().getAvatarPath())
                .createdAt(log.getCreatedAt())
                .build()
        ).collect(Collectors.toList());
    }
}  
