package com.openclaw.observer.controller.otel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.common.util.ClientIpUtils;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.dto.OtlpResponse;
import com.openclaw.observer.service.OtlpParserService;
import com.openclaw.observer.service.RawDataProcessorService;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class OtlpReceiverController {

    private static final String LOG_DIR = "data";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OtlpParserService otlpParserService;
    private final RawDataProcessorService rawDataProcessorService;
    private final ObjectMapper objectMapper;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
            .preservingProtoFieldNames()
            .includingDefaultValueFields();

    @PostMapping("/traces")
    public ResponseEntity<OtlpResponse> receiveTraces(HttpServletRequest request) throws Exception {
        return receiveData(request, "traces");
    }

    @PostMapping("/metrics")
    public ResponseEntity<OtlpResponse> receiveMetrics(HttpServletRequest request) throws Exception {
        return receiveData(request, "metrics");
    }

    @PostMapping("/logs")
    public ResponseEntity<OtlpResponse> receiveLogs(HttpServletRequest request) throws Exception {
        return receiveData(request, "logs");
    }

    private ResponseEntity<OtlpResponse> receiveData(HttpServletRequest request, String type) throws Exception {
        byte[] rawData = request.getInputStream().readAllBytes();
        LocalDateTime receivedAt = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        String clientIp = ClientIpUtils.getClientIp(request);
        String rawJson = parseToJson(rawData, type);

        log.info("\n" +
                "═══════════════════════════════════════════════════════════════\n" +
                "📨 收到 OTLP " + type.toUpperCase() + " 数据!\n" +
                "═══════════════════════════════════════════════════════════════\n" +
                "🕐 时间: " + receivedAt + "\n" +
                "📍 客户端IP: " + clientIp + "\n" +
                "📊 数据类型: " + type + "\n" +
                "📏 数据大小: " + rawData.length + " bytes\n" +
                "═══════════════════════════════════════════════════════════════\n");

        // 快速创建 RawDataDocument，不阻塞请求
        RawDataDocument rawDoc;
        switch (type) {
            case "logs" -> rawDoc = rawDataProcessorService.createForOtlpLogs(rawJson, clientIp);
            case "metrics" -> rawDoc = rawDataProcessorService.createForOtlpMetrics(rawJson, clientIp);
            case "traces" -> rawDoc = rawDataProcessorService.createForOtlpTraces(rawJson, clientIp);
            default -> throw new IllegalArgumentException("未知的类型: " + type);
        }

        log.info("✅ RawData 已创建 (id: {})，后台线程将自动处理", rawDoc.getId());

        // 保存原始文件（保留原有逻辑）
        saveToFile(rawData, type);

        return OtlpResponse.success(type, rawData.length).toResponseEntity();
    }

    private String parseToJson(byte[] rawData, String type) throws Exception {
        try {
            switch (type) {
                case "traces":
                    ExportTraceServiceRequest traceRequest = ExportTraceServiceRequest.parseFrom(rawData);
                    return jsonPrinter.print(traceRequest);
                case "metrics":
                    ExportMetricsServiceRequest metricRequest = ExportMetricsServiceRequest.parseFrom(rawData);
                    return jsonPrinter.print(metricRequest);
                case "logs":
                    ExportLogsServiceRequest logRequest = ExportLogsServiceRequest.parseFrom(rawData);
                    return jsonPrinter.print(logRequest);
            }
        } catch (Exception e) {
            log.warn("⚠️ 转换 JSON 失败，使用 otlpParserService", e);
        }
        return otlpParserService.parseToJson(rawData, type);
    }

    private void saveToFile(byte[] data, String type) {
        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String dateStr = LocalDateTime.now().format(FILE_DATE_FORMAT);
            String fileName = String.format("%s/%s-%s.raw", LOG_DIR, dateStr, type);

            Path rawFilePath = Paths.get(fileName);
            Files.write(
                    rawFilePath,
                    data,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            log.debug("💾 原始PB数据已保存到文件: " + fileName);

        } catch (Exception e) {
            log.warn("⚠️ 保存数据到文件失败: " + e.getMessage(), e);
        }
    }
}
