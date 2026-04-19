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
public class SessionUploadResponse {
    private boolean success;
    private String sessionId;
    private int processedCount;
    private List<String> processedEventIds;
    private String error;

    public static SessionUploadResponse success(String sessionId, int count, List<String> eventIds) {
        return SessionUploadResponse.builder()
            .success(true)
            .sessionId(sessionId)
            .processedCount(count)
            .processedEventIds(eventIds)
            .build();
    }

    public static SessionUploadResponse error(String error) {
        return SessionUploadResponse.builder()
            .success(false)
            .error(error)
            .build();
    }
}
