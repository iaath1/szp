package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MyProjectsStatsDTO {
    private long total;
    private long completed;
    private long inProggres;
    private long onHold;
}
