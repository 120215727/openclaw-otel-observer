package com.openclaw.observer.dto;

import com.openclaw.observer.document.OtelMetricDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponse {
    private String id;
    private String metricName;
    private String metricType;
    private String serviceName;
    private Double valueDouble;
    private Long valueLong;
    private Integer valueInt;
    private String createdAt;

    public static MetricResponse from(OtelMetricDocument doc) {
        if (doc == null) return null;
        return MetricResponse.builder()
            .id(doc.getId())
            .metricName(doc.getMetricName())
            .metricType(doc.getMetricType())
            .serviceName(doc.getServiceName())
            .valueDouble(doc.getValueDouble())
            .valueLong(doc.getValueLong())
            .valueInt(doc.getValueInt())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
