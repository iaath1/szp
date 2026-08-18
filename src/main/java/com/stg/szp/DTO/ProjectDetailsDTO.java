package com.stg.szp.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stg.szp.models.ProjectStatus;
import com.stg.szp.models.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDetailsDTO {
    private Long id;

    private String title;

    private String description;

    private UserResponseDTO owner;

    private LocalDateTime createdAt = LocalDateTime.now();

    private ProjectStatus status;

    private Long proggress;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    private LocalDateTime startAt;

    private LocalDateTime deadlineAt;

    private int membersCount;
    private int tasksCount;
    private List<TagDTO> tags;
    // filesCount and commentsCounts needs to be added


}
