package com.openclaw.observer.exception;

import com.openclaw.observer.controller.collector.SessionReceiverController;
import com.openclaw.observer.dto.SessionUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 专门处理 SessionReceiverController 的异常
 */
@Slf4j
@RestControllerAdvice(assignableTypes = SessionReceiverController.class)
public class SessionControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<SessionUploadResponse> handleSessionException(Exception e) {
        log.error("❌ 处理 Session 数据失败: " + e.getMessage(), e);
        return ResponseEntity.badRequest().body(SessionUploadResponse.error(e.getMessage()));
    }
}
