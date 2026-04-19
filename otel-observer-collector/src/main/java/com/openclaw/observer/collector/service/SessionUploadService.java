package com.openclaw.observer.collector.service;

import com.openclaw.observer.common.enums.SessionFileType;
import com.openclaw.observer.dto.SessionDataUploadRequest;
import com.openclaw.observer.dto.SessionUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionUploadService {

    private final WebClient receiverWebClient;

    public boolean uploadSessionMetadata(String agentId, String sessionId, String fileName, String dataJson) {
        try {
            SessionDataUploadRequest request = SessionDataUploadRequest.forMetadata(agentId, sessionId, fileName, dataJson);

            log.info("上传 Session Metadata 到 Receiver (session: {}, fileName: {})", sessionId, fileName);

            receiverWebClient.post().uri("/api/v1/sessions/data").bodyValue(request).retrieve().bodyToMono(SessionUploadResponse.class).doOnSuccess(response -> {
                if (response.isSuccess()) {
                    log.info("✅ Session Metadata 上传成功: 处理了 {} 条记录", response.getProcessedCount());
                } else {
                    log.warn("⚠️ Session Metadata 上传失败: {}", response.getError());
                }
            }).doOnError(e -> log.error("❌ Session Metadata 上传异常: {}", e.getMessage())).block();

            return true;

        } catch (Exception e) {
            log.error("❌ 上传 Session Metadata 失败: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean uploadSessionEvent(String agentId, String sessionId, SessionFileType fileType, String fileName, Long lineNo, String dataJson) {
        try {
            SessionDataUploadRequest request = SessionDataUploadRequest.forEvent(agentId, sessionId, fileType, fileName, lineNo, dataJson);

            receiverWebClient.post().uri("/api/v1/sessions/data").bodyValue(request).retrieve().bodyToMono(SessionUploadResponse.class).doOnSuccess(response -> {
                if (response.isSuccess()) {
                    log.debug("✅ Session Event 上传成功 (lineNo: {})", lineNo);
                } else {
                    log.warn("⚠️ Session Event 上传失败: {}", response.getError());
                }
            }).doOnError(e -> log.error("❌ Session Event 上传异常: {}", e.getMessage())).block();

            return true;

        } catch (Exception e) {
            log.error("❌ 上传 Session Event 失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
