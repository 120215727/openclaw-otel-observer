package com.openclaw.observer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long traceCount;
    private long metricCount;
    private long logCount;
    private long sessionCount;
    private LogLevelDistributionResponse logLevels;
    private RecentActivityResponse recentActivity;
    private List<MetricCountResponse> topMetrics;
}
