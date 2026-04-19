package com.openclaw.observer.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Raw Data 类型枚举
 *
 * 定义所有支持的原始数据类型
 */
public enum RawType {

    /**
     * OTLP Logs 数据
     */
    OTLP_LOGS("OTLP Logs"),

    /**
     * OTLP Metrics 数据
     */
    OTLP_METRICS("OTLP Metrics"),

    /**
     * OTLP Traces 数据
     */
    OTLP_TRACES("OTLP Traces"),

    /**
     * Session JSONL 文件数据
     */
    SESSION_JSONL("Session JSONL"),

    /**
     * Session 上传数据
     */
    SESSION_UPLOAD("Session Upload"),

    /**
     * Session 元数据（sessions.json）
     */
    SESSION_METADATA("Session Metadata"),

    /**
     * Session 单条事件
     */
    SESSION_EVENT("Session Event");

    /**
     * 显示名称
     */
    private final String displayName;

    private RawType(String displayName) {
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
    public static RawType fromValue(String value) {
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
     * 判断是否为 OTLP 类型
     *
     * @return true 如果是 OTLP 类型
     */
    public boolean isOtlpType() {
        return this == OTLP_LOGS || this == OTLP_METRICS || this == OTLP_TRACES;
    }

    /**
     * 判断是否为 Session 类型
     *
     * @return true 如果是 Session 类型
     */
    public boolean isSessionType() {
        return this == SESSION_JSONL || this == SESSION_UPLOAD
            || this == SESSION_METADATA || this == SESSION_EVENT;
    }
}
