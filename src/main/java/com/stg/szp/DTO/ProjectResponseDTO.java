package com.stg.szp.DTO;

import java.time.LocalDateTime;

import com.stg.szp.models.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponseDTO {
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private UserResponseDTO owner;
    private ProjectStatus status;
    private boolean isPrivate;
    private LocalDateTime startAt;
    private LocalDateTime deadlineAt;
}
