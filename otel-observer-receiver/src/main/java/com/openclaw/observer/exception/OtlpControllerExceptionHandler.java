package com.openclaw.observer.exception;

import com.openclaw.observer.controller.otel.OtlpReceiverController;
import com.openclaw.observer.dto.OtlpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 专门处理 OtlpReceiverController 的异常
 */
@Slf4j
@RestControllerAdvice(assignableTypes = OtlpReceiverController.class)
public class OtlpControllerExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OtlpResponse> handleOtlpException(Exception e) {
        log.error("❌ 处理 OTLP 数据失败: " + e.getMessage(), e);
        return OtlpResponse.error(e.getMessage()).toResponseEntity();
    }
}
