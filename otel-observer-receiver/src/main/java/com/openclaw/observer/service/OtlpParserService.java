package com.openclaw.observer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtlpParserService {

    private final ObjectMapper objectMapper;
    private final JsonFormat.Printer jsonPrinter = JsonFormat.printer()
            .preservingProtoFieldNames()
            .includingDefaultValueFields();

    /**
     * 解析OTLP Trace数据为JSON
     */
    public String parseTraceToJson(byte[] data) {
        try {
            ExportTraceServiceRequest request = ExportTraceServiceRequest.parseFrom(data);
            return protoToJson(request);
        } catch (InvalidProtocolBufferException e) {
            log.warn("Failed to parse OTLP trace data: {}", e.getMessage());
            return handleParseError(data, e);
        }
    }

    /**
     * 解析OTLP Metric数据为JSON
     */
    public String parseMetricToJson(byte[] data) {
        try {
            ExportMetricsServiceRequest request = ExportMetricsServiceRequest.parseFrom(data);
            return protoToJson(request);
        } catch (InvalidProtocolBufferException e) {
            log.warn("Failed to parse OTLP metric data: {}", e.getMessage());
            return handleParseError(data, e);
        }
    }

    /**
     * 解析OTLP Log数据为JSON
     */
    public String parseLogToJson(byte[] data) {
        try {
            ExportLogsServiceRequest request = ExportLogsServiceRequest.parseFrom(data);
            return protoToJson(request);
        } catch (InvalidProtocolBufferException e) {
            log.warn("Failed to parse OTLP log data: {}", e.getMessage());
            return handleParseError(data, e);
        }
    }

    /**
     * 自动检测类型并解析为JSON
     */
    public String parseToJson(byte[] data, String type) {
        try {
            return switch (type.toLowerCase()) {
                case "traces" -> parseTraceToJson(data);
                case "metrics" -> parseMetricToJson(data);
                case "logs" -> parseLogToJson(data);
                default -> {
                    log.warn("Unknown OTLP type: {}, trying all parsers", type);
                    yield tryParseAll(data);
                }
            };
        } catch (Exception e) {
            log.warn("Failed to parse OTLP {} data: {}", type, e.getMessage());
            return handleParseError(data, e);
        }
    }

    private String protoToJson(Message message) throws InvalidProtocolBufferException {
        return jsonPrinter.print(message);
    }

    private String tryParseAll(byte[] data) {
        // Try Trace first
        try {
            ExportTraceServiceRequest request = ExportTraceServiceRequest.parseFrom(data);
            if (request.getResourceSpansCount() > 0) {
                return protoToJson(request);
            }
        } catch (Exception ignored) {}

        // Try Metric
        try {
            ExportMetricsServiceRequest request = ExportMetricsServiceRequest.parseFrom(data);
            if (request.getResourceMetricsCount() > 0) {
                return protoToJson(request);
            }
        } catch (Exception ignored) {}

        // Try Log
        try {
            ExportLogsServiceRequest request = ExportLogsServiceRequest.parseFrom(data);
            if (request.getResourceLogsCount() > 0) {
                return protoToJson(request);
            }
        } catch (Exception ignored) {}

        return handleParseError(data, new Exception("Could not parse as any OTLP type"));
    }

    private String handleParseError(byte[] data, Exception e) {
        try {
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("parse_error", e.getMessage());
            errorNode.put("raw_data_size", data.length);
            errorNode.put("raw_data_base64", java.util.Base64.getEncoder().encodeToString(data));
            return objectMapper.writeValueAsString(errorNode);
        } catch (Exception jsonEx) {
            return "{\"error\":\"" + e.getMessage() + "\",\"data_size\":" + data.length + "}";
        }
    }
}
