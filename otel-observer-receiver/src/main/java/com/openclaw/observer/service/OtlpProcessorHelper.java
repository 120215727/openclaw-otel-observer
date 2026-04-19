package com.openclaw.observer.service;

import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtlpProcessorHelper {

    private OtlpProcessorHelper() {}

    public static Object anyValueToObject(AnyValue value) {
        if (value.hasStringValue()) return value.getStringValue();
        if (value.hasBoolValue()) return value.getBoolValue();
        if (value.hasIntValue()) return value.getIntValue();
        if (value.hasDoubleValue()) return value.getDoubleValue();
        if (value.hasArrayValue()) {
            List<Object> list = new ArrayList<>();
            for (AnyValue v : value.getArrayValue().getValuesList()) {
                list.add(anyValueToObject(v));
            }
            return list;
        }
        if (value.hasKvlistValue()) {
            return attributesToMap(value.getKvlistValue().getValuesList());
        }
        return null;
    }

    public static String anyValueToString(AnyValue value) {
        Object obj = anyValueToObject(value);
        return obj != null ? obj.toString() : "";
    }

    public static Map<String, Object> attributesToMap(List<KeyValue> attributes) {
        Map<String, Object> map = new java.util.HashMap<>();
        for (KeyValue attr : attributes) {
            map.put(attr.getKey(), anyValueToObject(attr.getValue()));
        }
        return map;
    }

    public static String hashAttributes(List<KeyValue> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "empty";
        }
        try {
            StringBuilder sb = new StringBuilder();
            for (KeyValue kv : attributes) {
                sb.append(kv.getKey());
                sb.append("=");
                sb.append(anyValueToString(kv.getValue()));
                sb.append("|");
            }
            return hash(sb.toString());
        } catch (Exception e) {
            return String.valueOf(attributes.hashCode());
        }
    }

    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(input.hashCode());
        }
    }

    public static LocalDateTime nanosToLocalDateTime(long nanos, ZoneId zoneId) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(nanos / 1_000_000_000, nanos % 1_000_000_000),
                zoneId
        );
    }

    public static String formatDate(LocalDateTime dateTime, DateTimeFormatter formatter) {
        if (dateTime == null) return null;
        return dateTime.format(formatter);
    }

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String getAsString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    public static Long getAsLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
