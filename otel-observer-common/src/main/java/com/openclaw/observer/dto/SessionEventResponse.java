package com.openclaw.observer.dto;

import com.openclaw.observer.document.SessionEventDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionEventResponse {
    private String id;
    private String sessionId;
    private String agentId;
    private String agentName;
    private String eventType;
    private String eventId;
    private String eventTimestamp;
    private String messageRole;
    private String messageText;
    private String messageModel;
    private String messageProvider;
    private String messageStopReason;
    private String modelProvider;
    private String modelId;
    private String thinkingLevel;
    private String customType;
    private String createdAt;

    public static SessionEventResponse from(SessionEventDocument doc) {
        if (doc == null) return null;
        return SessionEventResponse.builder()
            .id(doc.getId())
            .sessionId(doc.getSessionId())
            .agentId(doc.getAgentId())
            .agentName(doc.getAgentName())
            .eventType(doc.getEventType())
            .eventId(doc.getEventId())
            .eventTimestamp(doc.getEventTimestamp())
            .messageRole(doc.getMessageRole())
            .messageText(doc.getMessagePreview())
            .modelProvider(doc.getModelProvider())
            .modelId(doc.getModelId())
            .thinkingLevel(doc.getThinkingLevel())
            .customType(doc.getCustomType())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
