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
public class SessionUploadRequest {

    private String agentId;
    private String agentName;
    private String sessionId;
    private List<SessionEvent> events;

}
