package com.stg.szp.DTO;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ReportResponseDTO {
    private MetricsDTO metrics;
    private List<TimeDataDTO> timeData;
    private List<ProjectDataDTO> projectData;

    @Data
    @Builder
    public static class MetricsDTO {
        private int completedTasks;
        private int productivityScore;
        private int overdueTasks;
    }

    @Data
    @Builder
    public static class TimeDataDTO {
        private String name;
        private int tasks;
    }

    @Data
    @Builder
    public static class ProjectDataDTO {
        private String name;
        private int value;
    }
}
