package com.stg.szp.DTO;

import java.time.LocalDateTime;

import org.springframework.cglib.core.Local;

import com.stg.szp.models.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditProjectDTO {
    private String title;
    private LocalDateTime startAt;
    private LocalDateTime deadlineAt;
    private String projectKey;
    private String description;
    private ProjectStatus status;
}
