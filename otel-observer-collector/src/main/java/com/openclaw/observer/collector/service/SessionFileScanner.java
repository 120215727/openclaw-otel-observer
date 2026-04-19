package com.openclaw.observer.collector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.collector.model.SessionRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionFileScanner {

    private final ObjectMapper objectMapper;

    /**
     * 解析 sessions.json，获取完整的 session 记录
     */
    public Map<String, SessionRecord> parseFullSessionRecordsFromSessionsJson(File sessionsDir) {
        Map<String, SessionRecord> sessionRecords = new HashMap<>();
        File sessionsJson = new File(sessionsDir, "sessions.json");
        if (!sessionsJson.exists()) {
            return sessionRecords;
        }
        try {
            String json = Files.readString(sessionsJson.toPath());
            JsonNode root = objectMapper.readTree(json);
            Iterator<String> fieldNames = root.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode sessionNode = root.get(key);
                if (sessionNode.has("sessionId")) {
                    String sessionId = sessionNode.get("sessionId").asText();
                    long updatedAt = sessionNode.has("updatedAt") ? sessionNode.get("updatedAt").asLong() : 0;
                    String sessionJson = objectMapper.writeValueAsString(sessionNode);
                    sessionRecords.put(sessionId, new SessionRecord(sessionId, updatedAt, sessionJson));
                }
            }
        } catch (IOException e) {
            log.warn("解析 sessions.json 失败: {}", e.getMessage());
        }
        return sessionRecords;
    }
}
