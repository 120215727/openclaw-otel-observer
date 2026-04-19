package com.openclaw.observer.dto;

import com.openclaw.observer.document.SessionDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {
    private String id;
    private String sessionId;
    private String agentId;
    private String createdAt;

    public static SessionResponse from(SessionDocument doc) {
        if (doc == null) return null;
        return SessionResponse.builder()
            .id(doc.getId())
            .sessionId(doc.getSessionId())
            .agentId(doc.getAgentId())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
