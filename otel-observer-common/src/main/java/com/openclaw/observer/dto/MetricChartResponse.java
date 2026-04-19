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
public class MetricChartResponse {
    private List<String> times;
    private List<Double> values;
    private String metricName;
}
