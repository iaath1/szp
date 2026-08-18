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
public class CalendarEventRequestDTO {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EventType type;
    private Long projectId;
}
