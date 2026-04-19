package com.openclaw.observer.service.processor.otel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.openclaw.observer.common.ProcessResult;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.service.processor.RawDataProcessor;
import io.opentelemetry.proto.common.v1.KeyValue;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Raw OTLP 数据处理器抽象基类
 * <p>
 * 抽取 OtlpLogsProcessor、OtlpTracesProcessor、OtlpMetricsProcessor 的公共代码
 * 直接处理 RawDataDocument，避免多线程问题
 */
@Slf4j
public abstract class AbstractRawOtlpProcessor<T> implements RawDataProcessor {

    protected static final ZoneId SHANGHAI_ZONE = ZoneId.of("Asia/Shanghai");
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    protected final ObjectMapper objectMapper;
    protected final JsonFormat.Parser jsonParser = JsonFormat.parser();

    protected AbstractRawOtlpProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void process(RawDataDocument rawDoc) {
        log.info("处理 {} (id: {})", getTypeLabel(), rawDoc.getId());

        try {
            T request = parseRequest(rawDoc.getRawData());
            ProcessResult result = doProcess(rawDoc, request);
            logResult(result);

        } catch (Exception e) {
            log.error("处理 {} 失败: {}", getTypeLabel(), e.getMessage(), e);
            throw new RuntimeException("处理 " + getTypeLabel() + " 失败", e);
        }
    }

    /**
     * 解析 rawJson 为具体的 Request 类型
     */
    protected abstract T parseRequest(String rawJson) throws Exception;

    /**
     * 实际处理逻辑
     */
    protected abstract ProcessResult doProcess(RawDataDocument rawDoc, T request) throws Exception;

    /**
     * 获取类型名称（用于日志）
     */
    protected abstract String getTypeLabel();

    /**
     * 记录处理结果日志
     */
    protected abstract void logResult(ProcessResult result);

    // ==================== 公共辅助方法 ====================

    /**
     * 将 KeyValue 列表转换为 Map
     */
    protected Map<String, Object> attributesToMap(List<KeyValue> attributes) {
        Map<String, Object> map = new HashMap<>();
        for (KeyValue attr : attributes) {
            map.put(attr.getKey(), anyValueToObject(attr.getValue()));
        }
        return map;
    }

    /**
     * 从资源属性中获取服务名称
     */
    protected String getServiceName(Map<String, Object> resourceAttrs) {
        Object name = resourceAttrs.get("service.name");
        return name != null ? name.toString() : "unknown";
    }

    /**
     * 获取字符串属性
     */
    protected String getStrAttr(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取长整型属性
     */
    protected Long getLongAttr(Map<String, Object> attrs, String key) {
        Object value = attrs.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 将时间戳转换为 LocalDateTime
     */
    protected LocalDateTime nanosToLocalDateTime(long nanos) {
        return LocalDateTime.ofInstant(
            java.time.Instant.ofEpochSecond(nanos / 1_000_000_000, nanos % 1_000_000_000),
            SHANGHAI_ZONE
        );
    }

    /**
     * 格式化日期
     */
    protected String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 字节数组转十六进制字符串
     */
    protected String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 哈希字符串
     */
    protected String hash(String input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16);
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    /**
     * 哈希属性列表
     */
    protected String hashAttributes(List<KeyValue> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        for (KeyValue attr : attributes) {
            sb.append(attr.getKey()).append("=");
            if (attr.hasValue()) {
                sb.append(anyValueToString(attr.getValue()));
            }
            sb.append("|");
        }
        return String.valueOf(sb.toString().hashCode());
    }

    /**
     * AnyValue 转 String
     */
    protected String anyValueToString(io.opentelemetry.proto.common.v1.AnyValue value) {
        if (value == null) {
            return "";
        }
        switch (value.getValueCase()) {
            case STRING_VALUE:
                return value.getStringValue();
            case INT_VALUE:
                return String.valueOf(value.getIntValue());
            case DOUBLE_VALUE:
                return String.valueOf(value.getDoubleValue());
            case BOOL_VALUE:
                return String.valueOf(value.getBoolValue());
            default:
                return value.toString();
        }
    }

    /**
     * AnyValue 转 Object
     */
    protected Object anyValueToObject(io.opentelemetry.proto.common.v1.AnyValue value) {
        if (value == null) {
            return null;
        }
        switch (value.getValueCase()) {
            case STRING_VALUE:
                return value.getStringValue();
            case INT_VALUE:
                return value.getIntValue();
            case DOUBLE_VALUE:
                return value.getDoubleValue();
            case BOOL_VALUE:
                return value.getBoolValue();
            default:
                return value.toString();
        }
    }
}
