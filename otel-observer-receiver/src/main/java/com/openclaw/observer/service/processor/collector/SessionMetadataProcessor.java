package com.openclaw.observer.service.processor.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.document.SessionDocument;
import com.openclaw.observer.dto.SessionDataUploadRequest;
import com.openclaw.observer.dto.SessionMetadata;
import com.openclaw.observer.repository.SessionEsRepository;
import com.openclaw.observer.service.processor.RawDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 处理 SESSION_METADATA 类型的 RawData
 * 从 SessionDataUploadRequest 中提取 dataJson 并保存到 SessionDocument
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionMetadataProcessor implements RawDataProcessor {

    private final SessionEsRepository sessionEsRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void process(RawDataDocument rawDoc) {
        log.info("处理 SESSION_METADATA (sessionId: {}, agentId: {})", rawDoc.getSessionId(), rawDoc.getAgentId());

        try {
            // 1. 反序列化 rawData 为 SessionDataUploadRequest
            SessionDataUploadRequest uploadRequest = objectMapper.readValue(rawDoc.getRawData(), SessionDataUploadRequest.class);
            String dataJson = uploadRequest.getDataJson();
            if (dataJson == null || dataJson.isEmpty()) {
                log.warn("SESSION_METADATA 中没有 dataJson，跳过处理");
                return;
            }

            // 2. 解析 dataJson 为 SessionMetadata DTO
            SessionMetadata metadata = objectMapper.readValue(dataJson, SessionMetadata.class);

            // 3. 查找或创建 SessionDocument
            String sessionId = rawDoc.getSessionId();
            String agentId = rawDoc.getAgentId();

            Optional<SessionDocument> existingSession = sessionEsRepository.findByAgentIdAndSessionId(agentId, sessionId);
            SessionDocument sessionDoc = existingSession.orElseGet(() -> createNewSession(sessionId, agentId, rawDoc));

            // 4. 更新 SessionDocument 信息
            updateSessionFromMetadata(sessionDoc, metadata, rawDoc);

            // 5. 保存
            sessionEsRepository.save(sessionDoc);

            log.info("✅ SESSION_METADATA 处理完成 (sessionId: {})", sessionId);

        } catch (Exception e) {
            log.error("处理 SESSION_METADATA 失败 (id: {})", rawDoc.getId(), e);
            throw new RuntimeException("处理 SESSION_METADATA 失败", e);
        }
    }

    private SessionDocument createNewSession(String sessionId, String agentId, RawDataDocument rawDoc) {
        SessionDocument sessionDoc = new SessionDocument();
        sessionDoc.setId(generateSessionDocId(agentId, sessionId));
        sessionDoc.setSessionId(sessionId);
        sessionDoc.setAgentId(agentId);
        sessionDoc.setCreatedAt(rawDoc.getCreatedAt());
        return sessionDoc;
    }

    private void updateSessionFromMetadata(SessionDocument sessionDoc, SessionMetadata metadata, RawDataDocument rawDoc) {
        // rawDataId
        sessionDoc.setRawDataId(rawDoc.getId());

        // 基础信息
        sessionDoc.setSessionFile(metadata.getSessionFile());
        sessionDoc.setCompactionCount(metadata.getCompactionCount());
        sessionDoc.setLastChannel(metadata.getLastChannel());
        sessionDoc.setChatType(metadata.getChatType());
        sessionDoc.setRuntimeMs(metadata.getRuntimeMs());

        // 时间戳
        if (metadata.getUpdatedAt() != null) {
            LocalDateTime updatedAt = LocalDateTime.ofEpochSecond(
                metadata.getUpdatedAt() / 1000,
                (int) (metadata.getUpdatedAt() % 1000) * 1_000_000,
                ZoneOffset.UTC
            );
            sessionDoc.setSessionTimestamp(updatedAt.format(DateTimeFormatter.ISO_DATE_TIME));
        }

        // 模型信息
        sessionDoc.setModelProvider(metadata.getModelProvider());
        sessionDoc.setModel(metadata.getModel());

        // deliveryContext
        if (metadata.getDeliveryContext() != null) {
            sessionDoc.setDeliveryChannel(metadata.getDeliveryContext().getChannel());
        }

        // origin 信息
        if (metadata.getOrigin() != null) {
            SessionMetadata.Origin origin = metadata.getOrigin();
            sessionDoc.setOriginLabel(origin.getLabel());
            sessionDoc.setOriginProvider(origin.getProvider());
            sessionDoc.setOriginSurface(origin.getSurface());
            sessionDoc.setOriginChatType(origin.getChatType());
        }

        // systemPromptReport 相关字段
        if (metadata.getSystemPromptReport() != null) {
            SessionMetadata.SystemPromptReport report = metadata.getSystemPromptReport();
            sessionDoc.setSessionKey(report.getSessionKey());
            sessionDoc.setWorkspaceDir(report.getWorkspaceDir());

            if (report.getSandbox() != null) {
                sessionDoc.setSandboxMode(report.getSandbox().getMode());
            }

            // injectedWorkspaceFileNames（空格分隔）
            if (report.getInjectedWorkspaceFiles() != null && !report.getInjectedWorkspaceFiles().isEmpty()) {
                String fileNames = report.getInjectedWorkspaceFiles().stream()
                    .map(SessionMetadata.InjectedWorkspaceFile::getName)
                    .collect(Collectors.joining(" "));
                sessionDoc.setInjectedWorkspaceFileNames(fileNames);
            }

            // skillNames（从 systemPromptReport，空格分隔）
            if (report.getSkills() != null
                && report.getSkills().getEntries() != null
                && !report.getSkills().getEntries().isEmpty()) {
                String skillNames = report.getSkills().getEntries().stream()
                    .map(SessionMetadata.SkillEntry::getName)
                    .collect(Collectors.joining(" "));
                sessionDoc.setSkillNames(skillNames);
            }

            // toolNames（空格分隔）
            if (report.getTools() != null
                && report.getTools().getEntries() != null
                && !report.getTools().getEntries().isEmpty()) {
                String toolNames = report.getTools().getEntries().stream()
                    .map(SessionMetadata.ToolEntry::getName)
                    .collect(Collectors.joining(" "));
                sessionDoc.setToolNames(toolNames);
            }
        }
    }

    private String generateSessionDocId(String agentId, String sessionId) {
        return agentId + "-" + sessionId;
    }
}
