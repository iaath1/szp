package com.stg.szp.DTO;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cglib.core.Local;

import com.stg.szp.models.ProjectStatus;
import com.stg.szp.models.ProjectTemplate;
import com.stg.szp.models.Tag;
import com.stg.szp.models.TaskStatus;

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
    private ProjectTemplate projectTemplate;
    private TaskStatus defaultTaskStatus;
    private boolean allowMembersInvite;
    private boolean enableTaskComments;
    private boolean allowFileUploads;
    private boolean publicLinkEnabled;
    private List<Tag> tags;
}
