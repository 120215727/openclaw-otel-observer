package com.openclaw.observer.collector.service.processor;

import com.openclaw.observer.collector.model.AgentState;
import com.openclaw.observer.collector.model.SessionRecord;
import com.openclaw.observer.collector.service.SessionFileScanner;
import com.openclaw.observer.collector.service.SessionUploadService;
import com.openclaw.observer.common.enums.SessionFileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

@Slf4j
@Component("collectorSessionMetadataProcessor")
@RequiredArgsConstructor
public class SessionMetadataProcessor {

    private static final String SESSIONS_JSON_FILE_NAME = "sessions.json";

    private final SessionFileScanner fileScanner;
    private final SessionUploadService uploadService;

    public void processSessionsJson(File sessionsDir, AgentState agentState) {
        File sessionsJson = new File(sessionsDir, SESSIONS_JSON_FILE_NAME);
        if (!sessionsJson.exists()) {
            return;
        }

        Map<String, SessionRecord> sessionRecords = fileScanner.parseFullSessionRecordsFromSessionsJson(sessionsDir);
        if (sessionRecords.isEmpty()) {
            log.debug("sessions.json 中没有 session 记录");
            return;
        }

        log.debug("sessions.json 中找到 {} 个 session", sessionRecords.size());

        int updatedCount = 0;
        for (Map.Entry<String, SessionRecord> entry : sessionRecords.entrySet()) {
            String sessionId = entry.getKey();
            SessionRecord record = entry.getValue();

            Long lastUpdatedAt = agentState.getSessionIdRecordUpdatedAt().get(sessionId);
            if (lastUpdatedAt == null || record.getUpdatedAt() > lastUpdatedAt) {
                log.info("Session {} 有变化 (updatedAt: {} -> {})", sessionId, lastUpdatedAt, record.getUpdatedAt());
                if (uploadSessionMetadata(agentState, sessionId, record.getSessionJson())) {
                    agentState.getSessionIdRecordUpdatedAt().put(sessionId, record.getUpdatedAt());
                    updatedCount++;
                }
            }
        }

        if (updatedCount > 0) {
            log.info("sessions.json 处理完成，更新了 {} 个 session", updatedCount);
        }
    }

    private boolean uploadSessionMetadata(AgentState agentState, String sessionId, String metadataJson) {
        log.info("上传 Session Metadata (session: {})", sessionId);
        return uploadService.uploadSessionMetadata(agentState.getAgentId(), sessionId, SESSIONS_JSON_FILE_NAME, metadataJson);
    }
}
