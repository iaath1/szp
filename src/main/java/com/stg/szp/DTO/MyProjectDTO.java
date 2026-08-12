package com.stg.szp.DTO;

import java.time.LocalDateTime;
import java.util.List;

import com.stg.szp.models.ProjectStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyProjectDTO {
    private long id;
    private String title;
    private String description;
    private ProjectStatus status;
    private Long progress;
    private LocalDateTime deadLineAt;
    private LocalDateTime updatedAt;
    private LocalDateTime startAt;
    private List<UserResponseDTO> members;
    private int membersCount;
    private List<TagDTO> tags;
}
