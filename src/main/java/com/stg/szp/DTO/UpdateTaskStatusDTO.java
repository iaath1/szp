package com.stg.szp.DTO;

import com.stg.szp.models.TaskStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UpdateTaskStatusDTO {
    private TaskStatus status;
}
