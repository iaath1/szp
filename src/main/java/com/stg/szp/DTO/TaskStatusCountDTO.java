package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class TaskStatusCountDTO {
    private long todo;
    private long inProgress;
    private long done;
    private long review;
    private long overdue;
    private long doneChange;
    private long inProggressChange;
    private long overdueChange;
    
}
