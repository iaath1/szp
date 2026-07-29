package com.stg.szp.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.stg.szp.models.ProjectStatus;

import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectDTO {
    
    private String title;
    private String description;
    private boolean isPrivate;
    private LocalDateTime startAt;
    private LocalDateTime deadlineAt;
    private ProjectStatus status = ProjectStatus.IN_PROGRESS;
}
