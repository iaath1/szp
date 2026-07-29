package com.stg.szp.DTO;

import java.time.LocalDateTime;

import com.stg.szp.models.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UpcomingTasksDTO {
    private long id;
    private String title;
    private String projectTitle;
    private LocalDateTime deadline;
    private TaskStatus status;
}
