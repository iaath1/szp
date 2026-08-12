package com.stg.szp.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskCommentDTO {
    private Long id;
    private String content;
    private String authorName;
    private String authorEmail;
    private String authorAvatarUrl;
    private LocalDateTime createdAt;
    
}
