package com.openclaw.observer.collector.model;

import lombok.Getter;

@Getter
public class SessionRecord {
    private String sessionId;
    private long updatedAt;
    private String sessionJson;

    public SessionRecord(String sessionId, long updatedAt, String sessionJson) {
        this.sessionId = sessionId;
        this.updatedAt = updatedAt;
        this.sessionJson = sessionJson;
    }

}