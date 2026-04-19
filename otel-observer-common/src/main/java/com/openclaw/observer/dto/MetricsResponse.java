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
public class MetricsResponse {
    private List<MetricResponse> items;
    private long total;
    private int page;
    private int size;
    private int totalPages;
    private List<String> metricNames;
}
