package com.openclaw.observer.dto;

import com.openclaw.observer.document.OtelLogDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogResponse {
    private String id;
    private String traceId;
    private String spanId;
    private String serviceName;
    private String logLevel;
    private String message;
    private String timestamp;
    private String createdAt;

    public static LogResponse from(OtelLogDocument doc) {
        if (doc == null) return null;
        return LogResponse.builder()
            .id(doc.getId())
            .traceId(doc.getTraceId())
            .spanId(doc.getSpanId())
            .serviceName(doc.getServiceName())
            .logLevel(doc.getLogLevel())
            .message(doc.getMessage())
            .timestamp(doc.getTimestamp())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
