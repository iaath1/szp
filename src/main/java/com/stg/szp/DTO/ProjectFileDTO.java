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
public class ProjectFileDTO {
    private Long id;
    private String name;
    private Long size;
    private String type;
    private LocalDateTime uploadDate;
    private String uploaderName;
    private Long taskId;
    private String taskTitle;
    private String fileUrl;
    private String projectName;
    private Long projectId;
}
