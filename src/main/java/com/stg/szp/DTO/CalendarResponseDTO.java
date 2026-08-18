package com.stg.szp.DTO;

import java.time.LocalDateTime;

import com.stg.szp.models.EventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CalendarResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime starTime;
    private LocalDateTime endTime;

    private String sourceType;
    private EventType displayType;
    private String projectName; // null if personal
}
