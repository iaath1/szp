package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class TeamWorkloadDTO {
    private Long id;
    private String name;
    private String email;
    private String avatarUrl;
    private int totalTasks;
    private int completedTasks;
    private int progress;
}
