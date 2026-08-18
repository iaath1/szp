package com.stg.szp.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.stg.szp.models.Project;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.TaskPriority;
import com.stg.szp.models.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskDetailsDTO {
    private Long id;
    private String title;
    private String projectTitle;

    // Need to be changed to Project key (e.g WEB-1 or smth)
    private Long projectId;
    
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String assigneeEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deadlineAt;
    private List<ProjectFileDTO> attachments;
    private Integer commentsCount;
    private List<SubtaskDTO> subtasks;
}
