package com.openclaw.observer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.common.ObserverConstants;
import com.openclaw.observer.common.enums.ProcessingStatus;
import com.openclaw.observer.common.enums.RawType;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.dto.SessionDataUploadRequest;
import com.openclaw.observer.repository.RawDataEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * RawData 处理服务（简化版 - 异步架构）
 * <p>
 * 职责：
 * 1. 快速创建 RawDataDocument（status = pending）- 不阻塞请求
 * 2. 手动重试 failed 的 RawData
 * <p>
 * 注意：实际处理由 RawDataBackgroundProcessor 后台线程完成
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RawDataProcessorService {

    private final RawDataEsRepository rawDataRepository;
    private final ObjectMapper objectMapper;

    // ==================== 快速创建方法（不阻塞请求）====================

    /**
     * 快速创建 RawDataDocument - OTLP Logs
     */
    @Transactional
    public RawDataDocument createForOtlpLogs(String rawJson, String clientIp) {
        return createRawDataDocument(RawType.OTLP_LOGS, rawJson, clientIp);
    }

    /**
     * 快速创建 RawDataDocument - OTLP Metrics
     */
    @Transactional
    public RawDataDocument createForOtlpMetrics(String rawJson, String clientIp) {
        return createRawDataDocument(RawType.OTLP_METRICS, rawJson, clientIp);
    }

    /**
     * 快速创建 RawDataDocument - OTLP Traces
     */
    @Transactional
    public RawDataDocument createForOtlpTraces(String rawJson, String clientIp) {
        return createRawDataDocument(RawType.OTLP_TRACES, rawJson, clientIp);
    }


    /**
     * 快速创建 RawDataDocument - Session Metadata (sessions.json)
     */
    @Transactional
    public RawDataDocument createForSessionMetadata(SessionDataUploadRequest uploadRequest, String clientIp) {

        // 生成可预测的文档ID: RawType + agent_id + session_id
        String docId = generateSessionMetadataId(uploadRequest.getAgentId(), uploadRequest.getSessionId());

        // 存储完整的请求信息
        String fullRequestJson = serializeRequestToJson(uploadRequest);

        RawDataDocument rawDoc = createRawDataDocumentWithId(RawType.SESSION_METADATA, docId, fullRequestJson, clientIp);

        rawDoc.setAgentId(uploadRequest.getAgentId());
        rawDoc.setSessionId(uploadRequest.getSessionId());

        return rawDataRepository.save(rawDoc);
    }

    /**
     * 快速创建 RawDataDocument - 单条 Session Event
     */
    @Transactional
    public RawDataDocument createForSessionEvent(SessionDataUploadRequest uploadRequest, String clientIp) {

        // 从 dataJson 中解析出 event/message id
        String eventId = extractEventIdFromDataJson(uploadRequest.getDataJson());

        // 生成可预测的文档ID: RawType + agent_id + session_id + event_id
        String docId = generateSessionEventId(uploadRequest.getAgentId(), uploadRequest.getSessionId(), eventId);

        // 存储完整的请求信息
        String fullRequestJson = serializeRequestToJson(uploadRequest);

        RawDataDocument rawDoc = createRawDataDocumentWithId(RawType.SESSION_EVENT, docId, fullRequestJson, clientIp);

        rawDoc.setAgentId(uploadRequest.getAgentId());
        rawDoc.setSessionId(uploadRequest.getSessionId());

        return rawDataRepository.save(rawDoc);
    }

    // ==================== 手动重试方法 ====================

    /**
     * 手动重试单个 RawData
     * 将 status 从 failed 改回 pending，后台线程会自动处理
     */
    @Transactional
    public RawDataDocument retryRawData(String rawDataId) {
        log.info("手动重试 RawData: {}", rawDataId);

        RawDataDocument rawDoc = rawDataRepository.findById(rawDataId)
            .orElseThrow(() -> new IllegalArgumentException("RawData 不存在: " + rawDataId));

        if (!ProcessingStatus.FAILED.equals(rawDoc.getProcessingStatus())) {
            throw new IllegalStateException("只有 failed 状态的 RawData 可以重试，当前状态: " + rawDoc.getProcessingStatus());
        }

        // 重置状态为 pending
        resetRawDataDocument(rawDoc);

        log.info("✅ RawData 已重置为 pending，后台线程将自动处理: {}", rawDataId);

        return rawDoc;
    }

    /**
     * 批量重试 failed 的 RawData
     */
    @Transactional
    public int retryAllFailedRawData() {
        log.info("批量重试所有 failed 的 RawData");

        List<RawDataDocument> failedList = rawDataRepository
            .findByProcessingStatusOrderByCreatedAtDesc(ProcessingStatus.FAILED);

        int count = 0;
        for (RawDataDocument rawDoc : failedList) {
            try {
                retryRawData(rawDoc.getId());
                count++;
            } catch (Exception e) {
                log.warn("重试 RawData 失败 (id: {}): {}", rawDoc.getId(), e.getMessage());
            }
        }

        log.info("✅ 已触发重试 {} 条 failed 的 RawData", count);
        return count;
    }

    // ==================== 查询方法 ====================

    /**
     * 获取所有 failed 的 RawData
     */
    public List<RawDataDocument> getFailedRawData() {
        return rawDataRepository.findByProcessingStatusOrderByCreatedAtDesc(ProcessingStatus.FAILED);
    }

    /**
     * 获取所有 pending 的 RawData
     */
    public List<RawDataDocument> getPendingRawData() {
        return rawDataRepository.findByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus.PENDING);
    }

    // ==================== 删除方法 ====================

    /**
     * 删除 RawData
     */
    @Transactional
    public void deleteRawData(String rawDataId) {
        log.info("删除 RawData: {}", rawDataId);

        RawDataDocument rawDoc = rawDataRepository.findById(rawDataId)
            .orElseThrow(() -> new IllegalArgumentException("RawData 不存在: " + rawDataId));

        rawDataRepository.delete(rawDoc);

        log.info("✅ 已删除 RawData: {}", rawDataId);
    }

    // ==================== 私有方法 ====================

    private RawDataDocument createRawDataDocument(RawType rawType, String rawData, String clientIp) {
        return createRawDataDocumentWithId(rawType, UUID.randomUUID().toString(), rawData, clientIp);
    }

    private RawDataDocument createRawDataDocumentWithId(RawType rawType, String docId, String rawData, String clientIp) {

        LocalDateTime now = LocalDateTime.now(ObserverConstants.ZONE_SHANGHAI);
        String nowStr = now.format(ObserverConstants.ES_DATE_FORMAT);

        RawDataDocument doc = new RawDataDocument();
        doc.setId(docId);
        doc.setRawType(rawType);
        doc.setClientIp(clientIp);
        doc.setRawData(rawData);
        doc.setProcessingStatus(ProcessingStatus.PENDING);
        doc.setCreatedAt(nowStr);
        doc.setUpdatedAt(nowStr);

        return rawDataRepository.save(doc);
    }

    /**
     * 生成 Session Metadata 的文档ID
     * 格式: SESSION_METADATA-{agentId}-{sessionId}
     */
    private String generateSessionMetadataId(String agentId, String sessionId) {
        return RawType.SESSION_METADATA.name() + "-" + agentId + "-" + sessionId;
    }

    /**
     * 生成 Session Event 的文档ID
     * 格式: SESSION_EVENT-{agentId}-{sessionId}-{eventId}
     */
    private String generateSessionEventId(String agentId, String sessionId, String eventId) {
        return RawType.SESSION_EVENT.name() + "-" + agentId + "-" + sessionId + "-" + eventId;
    }

    /**
     * 从 dataJson 中提取 event/message id
     */
    private String extractEventIdFromDataJson(String dataJson) {
        if (dataJson == null || dataJson.isEmpty()) {
            return UUID.randomUUID().toString();
        }
        try {
            JsonNode rootNode = objectMapper.readTree(dataJson);
            String id = rootNode.path("id").asText(null);
            return id != null ? id : UUID.randomUUID().toString();
        } catch (Exception e) {
            log.warn("无法从 dataJson 中提取 event id，使用 UUID: {}", e.getMessage());
            return UUID.randomUUID().toString();
        }
    }

    /**
     * 将 SessionDataUploadRequest 序列化为 JSON
     */
    private String serializeRequestToJson(SessionDataUploadRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            throw new RuntimeException("序列化 SessionDataUploadRequest 失败", e);
        }
    }

    private void resetRawDataDocument(RawDataDocument rawDoc) {
        LocalDateTime now = LocalDateTime.now(ObserverConstants.ZONE_SHANGHAI);

        rawDoc.setProcessingStatus(ProcessingStatus.PENDING);
        rawDoc.setProcessingStartedAt(null);
        rawDoc.setProcessingCompletedAt(null);
        rawDoc.setErrorMessage(null);
        rawDoc.setErrorStackTrace(null);
        rawDoc.setUpdatedAt(now.format(ObserverConstants.ES_DATE_FORMAT));

        rawDataRepository.save(rawDoc);
    }
}
