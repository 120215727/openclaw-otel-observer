package com.openclaw.observer.common.enums;

/**
 * 会话文件类型枚举
 */
public enum SessionFileType {

    /**
     * sessions.json - 会话元数据汇总文件
     */
    SESSIONS_JSON("sessions-json"),

    /**
     * {sessionId}.jsonl - 标准会话文件
     */
    SESSION_JSONL("session-jsonl"),

    /**
     * {sessionId}.jsonl.reset.{timestamp} - 重置备份文件
     */
    RESET_BACKUP("reset-backup"),

    /**
     * {sessionId}.jsonl.deleted.{timestamp} - 删除备份文件
     */
    DELETED_BACKUP("deleted-backup"),

    /**
     * 未知文件类型
     */
    UNKNOWN("unknown");

    private final String value;

    SessionFileType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static SessionFileType fromValue(String value) {
        for (SessionFileType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
