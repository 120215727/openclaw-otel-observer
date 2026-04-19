package com.openclaw.observer.dto;

import com.openclaw.observer.document.OtelTraceDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraceResponse {
    private String id;
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String serviceName;
    private String spanName;
    private String spanKind;
    private String startTime;
    private String endTime;
    private Long durationMs;
    private String statusCode;
    private String statusDescription;
    private String createdAt;

    public static TraceResponse from(OtelTraceDocument doc) {
        if (doc == null) return null;
        return TraceResponse.builder()
            .id(doc.getId())
            .traceId(doc.getTraceId())
            .spanId(doc.getSpanId())
            .parentSpanId(doc.getParentSpanId())
            .serviceName(doc.getServiceName())
            .spanName(doc.getSpanName())
            .spanKind(doc.getSpanKind())
            .startTime(doc.getStartTime())
            .endTime(doc.getEndTime())
            .durationMs(doc.getDurationMs())
            .statusCode(doc.getStatusCode())
            .statusDescription(doc.getStatusDescription())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
