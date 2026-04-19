package com.openclaw.observer.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

/**
 * OpenClaw Session Event Document - ES存储（简化版）
 * 
 * 用途：
 * - 记录非 message 类型的事件（model_change, thinking_level_change, custom）
 * - 事件时序分析
 * - 完整事件日志（包含 message 事件的引用）
 * 
 * 索引：oc-session-events
 * 粒度：1 条 / Event
 * 
 * 注意：Message 事件已拆分到 SessionMessageDocument，
 *       这里只记录 event_type != "message" 的事件，
 *       或者也可以保留所有事件，但 message 事件只存储引用。
 */
@Data
@Document(indexName = "oc-session-events")
public class SessionEventDocument {

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

    // ==================== 关联字段 ====================

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
     * Agent 名称
     */
    @Field(type = FieldType.Keyword, name = "agent_name")
    private String agentName;

    // ==================== 事件基础字段 ====================

    /**
     * 事件类型
     * session/message/model_change/thinking_level_change/custom
     */
    @Field(type = FieldType.Keyword, name = "event_type")
    private String eventType;

    /**
     * 事件 ID
     */
    @Field(type = FieldType.Keyword, name = "event_id")
    private String eventId;

    /**
     * 父事件 ID（构建事件树）
     */
    @Field(type = FieldType.Keyword, name = "parent_id")
    private String parentId;

    /**
     * 事件时间
     */
    @Field(type = FieldType.Date, name = "event_timestamp")
    private String eventTimestamp;

    /**
     * 事件序号（在 Session 内的顺序）
     */
    @Field(type = FieldType.Long, name = "event_sequence")
    private Long eventSequence;

    // ==================== Message 事件引用（当 event_type="message" 时）====================

    /**
     * 是否为 Message 事件
     */
    @Field(type = FieldType.Boolean, name = "is_message")
    private Boolean isMessage;

    /**
     * 关联的 Message Document ID（仅 event_type="message" 时）
     * 用于关联查询：从 Event → Message
     */
    @Field(type = FieldType.Keyword, name = "message_doc_id")
    private String messageDocId;

    /**
     * Message 角色（冗余，用于快速筛选）
     */
    @Field(type = FieldType.Keyword, name = "message_role")
    private String messageRole;

    /**
     * Message 文本预览（冗余，用于列表显示）
     */
    @Field(type = FieldType.Text, name = "message_preview")
    private String messagePreview;

    // ==================== Model Change 特有字段 ====================

    /**
     * 模型提供商
     */
    @Field(type = FieldType.Keyword, name = "model_provider")
    private String modelProvider;

    /**
     * 模型 ID
     */
    @Field(type = FieldType.Keyword, name = "model_id")
    private String modelId;

    // ==================== Thinking Level Change 特有字段 ====================

    /**
     * 思考级别
     */
    @Field(type = FieldType.Keyword, name = "thinking_level")
    private String thinkingLevel;

    // ==================== Custom 特有字段 ====================

    /**
     * 自定义类型
     */
    @Field(type = FieldType.Keyword, name = "custom_type")
    private String customType;

    /**
     * 自定义数据 JSON
     */
    @Field(type = FieldType.Text, name = "custom_data_json")
    private String customDataJson;

}
