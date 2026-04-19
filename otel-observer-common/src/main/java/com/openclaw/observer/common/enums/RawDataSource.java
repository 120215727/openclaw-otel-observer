package com.openclaw.observer.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 数据来源枚举
 *
 * 定义 RawData 的所有可能来源
 */
public enum RawDataSource {

    /**
     * OTEL Receiver - 接收 OpenTelemetry 数据
     */
    OTEL_RECEIVER("OTEL Receiver"),

    /**
     * Session Collector - 收集本机 Session 数据
     */
    SESSION_COLLECTOR("Session Collector"),

    /**
     * Session Upload - 用户手动上传 Session 数据
     */
    SESSION_UPLOAD("Session Upload"),

    /**
     * Manual Upload - 通用手动上传
     */
    MANUAL_UPLOAD("Manual Upload");

    /**
     * 显示名称
     */
    private final String displayName;

    private RawDataSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据字符串值查找枚举（用于 Jackson 反序列化）
     *
     * @param value 字符串值
     * @return 对应的枚举，如果找不到返回 null
     */
    @JsonCreator
    public static RawDataSource fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 判断是否为 OTEL 相关来源
     *
     * @return true 如果是 OTEL 相关来源
     */
    public boolean isOtelSource() {
        return this == OTEL_RECEIVER;
    }

    /**
     * 判断是否为 Session 相关来源
     *
     * @return true 如果是 Session 相关来源
     */
    public boolean isSessionSource() {
        return this == SESSION_COLLECTOR || this == SESSION_UPLOAD;
    }
}
