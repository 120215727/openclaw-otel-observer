package com.openclaw.observer.service.processor.collector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.common.ObserverConstants;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.document.SessionMessageDocument;
import com.openclaw.observer.document.nested.payload.*;
import com.openclaw.observer.dto.SessionDataUploadRequest;
import com.openclaw.observer.repository.SessionMessageEsRepository;
import com.openclaw.observer.service.processor.RawDataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 处理 SESSION_EVENT 类型的 RawData
 * 解析单条 JSON 事件并创建 SessionMessageDocument
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionMessageProcessor implements RawDataProcessor {

    private final SessionMessageEsRepository sessionMessageEsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void process(RawDataDocument rawDoc) {
        log.info("处理 SESSION_EVENT (sessionId: {}, agentId: {})",
            rawDoc.getSessionId(), rawDoc.getAgentId());

        try {
            SessionDataUploadRequest uploadRequest = objectMapper.readValue(rawDoc.getRawData(), SessionDataUploadRequest.class);
            String dataJson = uploadRequest.getDataJson();

            JsonNode rootNode = objectMapper.readTree(dataJson.trim());
            String type = rootNode.path("type").asText();
            String eventId = rootNode.path("id").asText();

            String now = LocalDateTime.now(ObserverConstants.ZONE_SHANGHAI)
                .format(ObserverConstants.ES_DATE_FORMAT);

            SessionMessageDocument msgDoc = new SessionMessageDocument();
            msgDoc.setId(rawDoc.getAgentId() + "-" + rawDoc.getSessionId() + "-" + eventId);
            msgDoc.setCreatedAt(now);
            msgDoc.setSessionId(rawDoc.getSessionId());
            msgDoc.setAgentId(rawDoc.getAgentId());
            msgDoc.setEventId(eventId);
            msgDoc.setParentId(rootNode.path("parentId").asText(null));
            msgDoc.setEventType(type);
            msgDoc.setEventTimestamp(rootNode.path("timestamp").asText(null));

            switch (type) {
                case "session" -> {
                    SessionPayload payload = new SessionPayload();
                    payload.setVersion(rootNode.path("version").asInt());
                    payload.setCwd(rootNode.path("cwd").asText(null));
                    String payloadText = "Session started: " + payload.getCwd();
                    payload.setText(payloadText);
                    msgDoc.setText(payloadText);
                    msgDoc.setSession(payload);
                }
                case "message" -> {
                    MessagePayload payload = new MessagePayload();
                    JsonNode messageNode = rootNode.path("message");
                    if (!messageNode.isMissingNode()) {
                        payload.setRole(messageNode.path("role").asText(null));
                        payload.setTimestamp(messageNode.path("timestamp").asLong());

                        JsonNode contentNode = messageNode.path("content");
                        if (contentNode.isArray()) {
                            payload.setRawContentJsonArray(objectMapper.writeValueAsString(contentNode));
                            String text = extractText(contentNode);
                            payload.setText(text);
                            msgDoc.setText(text);
                            payload.setContentType(determineContentType(contentNode));
                        }
                    }
                    msgDoc.setMessage(payload);
                }
                case "model_change" -> {
                    ModelChangePayload payload = new ModelChangePayload();
                    payload.setProvider(rootNode.path("provider").asText(null));
                    payload.setModelId(rootNode.path("modelId").asText(null));
                    String payloadText = "Model changed: " + payload.getProvider() + "/" + payload.getModelId();
                    payload.setText(payloadText);
                    msgDoc.setText(payloadText);
                    msgDoc.setModelChange(payload);
                }
                case "thinking_level_change" -> {
                    ThinkingLevelChangePayload payload = new ThinkingLevelChangePayload();
                    payload.setThinkingLevel(rootNode.path("thinkingLevel").asText(null));
                    String payloadText = "Thinking level: " + payload.getThinkingLevel();
                    payload.setText(payloadText);
                    msgDoc.setText(payloadText);
                    msgDoc.setThinkingLevelChange(payload);
                }
                case "custom" -> {
                    CustomPayload payload = new CustomPayload();
                    payload.setCustomType(rootNode.path("customType").asText(null));
                    JsonNode dataNode = rootNode.path("data");
                    if (!dataNode.isMissingNode()) {
                        payload.setDataJson(objectMapper.writeValueAsString(dataNode));
                    }
                    String payloadText = "Custom event: " + payload.getCustomType();
                    payload.setText(payloadText);
                    msgDoc.setText(payloadText);
                    msgDoc.setCustom(payload);
                }
            }

            sessionMessageEsRepository.save(msgDoc);
            log.info("✅ SessionMessage 保存成功 (eventId: {}, type: {})", eventId, type);

        } catch (Exception e) {
            log.error("处理 SESSION_EVENT 失败 (id: {})", rawDoc.getId(), e);
            throw new RuntimeException("处理 SESSION_EVENT 失败", e);
        }
    }

    private String extractText(JsonNode contentArray) {
        StringBuilder textBuilder = new StringBuilder();
        for (JsonNode contentItem : contentArray) {
            if ("text".equals(contentItem.path("type").asText())) {
                String text = contentItem.path("text").asText();
                text = filterSenderMetadata(text);
                if (text != null && !text.trim().isEmpty()) {
                    textBuilder.append(text).append("\n");
                }
            }
        }
        return textBuilder.toString().trim();
    }

    private String determineContentType(JsonNode contentArray) {
        if (!contentArray.isArray() || contentArray.isEmpty()) {
            return null;
        }
        JsonNode firstItem = contentArray.get(0);
        return firstItem.path("type").asText(null);
    }

    private String filterSenderMetadata(String text) {
        if (text == null) {
            return null;
        }
        if (text.startsWith("Sender (untrusted metadata):")) {
            int lastNewline = text.lastIndexOf('\n');
            if (lastNewline > 0 && lastNewline < text.length() - 1) {
                String lastLine = text.substring(lastNewline + 1).trim();
                if (lastLine.startsWith("[")) {
                    int endBracket = lastLine.indexOf(']');
                    if (endBracket > 0 && endBracket < lastLine.length() - 1) {
                        return lastLine.substring(endBracket + 1).trim();
                    }
                }
                return lastLine;
            }
        }
        return text;
    }
}
