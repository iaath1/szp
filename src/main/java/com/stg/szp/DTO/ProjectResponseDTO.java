package com.stg.szp.DTO;

import java.time.LocalDateTime;

import com.stg.szp.models.SZP_User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponseDTO {
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private SZP_User owner;
}
