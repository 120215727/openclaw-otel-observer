package com.openclaw.observer.document;

import com.openclaw.observer.document.nested.payload.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * OpenClaw Session Message Document - ES存储
 *
 * 用途：
 * - 对话历史查看
 * - 消息搜索
 * - 消息分析
 * - Token统计
 * - 效果分析（按消息聚合）
 *
 * 索引：oc-session-messages
 * 粒度：1 条 / Event（支持所有JSONL事件类型）
 */
@Data
@Document(indexName = "oc-session-messages")
public class SessionMessageDocument {

    @Id
    private String id;

    // ==================== 元数据（ES 管理用）====================

    /**
     * 文档创建时间（ES 写入时间）
     */
    @Field(type = FieldType.Date, name = "created_at")
    private String createdAt;

    /**
     * 原始 OTLP JSON 数据（保留用于调试）
     */
    @Field(type = FieldType.Keyword, name = "raw_data_id")
    private String rawDataId;

    // ==================== 关联信息 ====================

    /**
     * Session ID
     */
    @Field(type = FieldType.Keyword, name = "session_id")
    private String sessionId;

    /**
     * Agent ID
     */
    @Field(type = FieldType.Keyword, name = "agent_id")
    private String agentId;

    /**
     * 事件 ID（对应 原始文档的id字段）
     */
    @Field(type = FieldType.Keyword, name = "event_id")
    private String eventId;

    /**
     * 父消息 ID（构建对话树）
     */
    @Field(type = FieldType.Keyword, name = "parent_id")
    private String parentId;

    // ==================== 事件基础信息（所有类型通用）====================

    /**
     * 事件类型
     * session / message / model_change / thinking_level_change / custom
     */
    @Field(type = FieldType.Keyword, name = "event_type")
    private String eventType;

    /**
     * 事件时间戳
     */
    @Field(type = FieldType.Date, name = "event_timestamp")
    private String eventTimestamp;

    /**
     * 列表显示简介（从对应的 payload 中提取）
     */
    @Field(type = FieldType.Text, name = "text")
    private String text;

    // ==================== 事件详情对象（每种类型一个对象）====================

    /**
     * Session 事件详情（仅 event_type=session）
     */
    @Field(type = FieldType.Object, name = "session")
    private SessionPayload session;

    /**
     * Message 事件详情（仅 event_type=message）
     */
    @Field(type = FieldType.Object, name = "message")
    private MessagePayload message;

    /**
     * Model Change 事件详情（仅 event_type=model_change）
     */
    @Field(type = FieldType.Object, name = "model_change")
    private ModelChangePayload modelChange;

    /**
     * Thinking Level Change 事件详情（仅 event_type=thinking_level_change）
     */
    @Field(type = FieldType.Object, name = "thinking_level_change")
    private ThinkingLevelChangePayload thinkingLevelChange;

    /**
     * Custom 事件详情（仅 event_type=custom）
     */
    @Field(type = FieldType.Object, name = "custom")
    private CustomPayload custom;

}
