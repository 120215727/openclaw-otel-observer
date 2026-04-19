package com.openclaw.observer.controller.collector;

import com.openclaw.observer.common.enums.SessionFileType;
import com.openclaw.observer.common.util.ClientIpUtils;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.dto.SessionDataUploadRequest;
import com.openclaw.observer.dto.SessionUploadResponse;
import com.openclaw.observer.service.RawDataProcessorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionReceiverController {

    private final RawDataProcessorService rawDataProcessorService;

    /**
     * 统一上传会话数据（元数据或事件）
     */
    @PostMapping("/data")
    public ResponseEntity<SessionUploadResponse> uploadSessionData(
            HttpServletRequest request,
            @RequestBody SessionDataUploadRequest uploadRequest) {

        String clientIp = ClientIpUtils.getClientIp(request);
        SessionFileType fileType = uploadRequest.getFileType() != null ? uploadRequest.getFileType() : SessionFileType.UNKNOWN;

        log.info("\n" +
                "═══════════════════════════════════════════════════════════════\n" +
                "📨 收到 Session 数据上传!\n" +
                "═══════════════════════════════════════════════════════════════\n" +
                "🕐 时间: " + LocalDateTime.now() + "\n" +
                "📍 客户端IP: " + clientIp + "\n" +
                "👤 Agent ID: " + uploadRequest.getAgentId() + "\n" +
                "🔑 Session ID: " + uploadRequest.getSessionId() + "\n" +
                "📁 文件类型: " + fileType.getValue() + "\n" +
                "📄 文件名: " + uploadRequest.getFileName() + "\n" +
                (uploadRequest.getLineNo() != null ? "#️⃣ 行号: " + uploadRequest.getLineNo() + "\n" : "") +
                "═══════════════════════════════════════════════════════════════\n");

        if (SessionFileType.SESSIONS_JSON.equals(uploadRequest.getFileType())) {
            RawDataDocument rawDoc = rawDataProcessorService.createForSessionMetadata(uploadRequest, clientIp);
            log.info("✅ Session Metadata RawData 已创建 (id: {})", rawDoc.getId());
        } else {
            RawDataDocument rawDoc = rawDataProcessorService.createForSessionEvent(uploadRequest, clientIp);
            log.info("✅ Session Event RawData 已创建 (id: {}, lineNo: {})", rawDoc.getId(), uploadRequest.getLineNo());
        }

        return ResponseEntity.ok(SessionUploadResponse.success(
                uploadRequest.getSessionId(),
                1,
                Collections.emptyList()
        ));
    }
}
