package com.stg.szp.DTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stg.szp.models.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Set<UserResponseDTO> members = new HashSet<>();

    private List<Task> tasks = new ArrayList<>();
}
