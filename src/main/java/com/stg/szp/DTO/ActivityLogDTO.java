package com.stg.szp.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ActivityLogDTO {
    private Long id;
    private String type;
    private String message;
    private String projectName;
    private Long projectId;
    private String userName;
    private String userAvatarUrl;
    private LocalDateTime createdAt;
}
