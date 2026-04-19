package com.openclaw.observer.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

/**
 * OpenClaw Session Document - ES存储（增强版）
 * <p>
 * 用途：
 * - Session 列表看板
 * - Session 维度统计
 * - 对话效果分析（按 Session 聚合）
 * <p>
 * 索引：oc-sessions
 * 粒度：1 条 / Session
 */
@Data
@Document(indexName = "oc-sessions")
public class SessionDocument {

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

    // ==================== Session 基础信息 ====================

    /**
     * Session ID（UUID）
     */
    @Field(type = FieldType.Keyword, name = "session_id")
    private String sessionId;

    /**
     * Agent ID
     */
    @Field(type = FieldType.Keyword, name = "agent_id")
    private String agentId;


    /**
     * Session 创建时间
     */
    @Field(type = FieldType.Date, name = "session_timestamp")
    private String sessionTimestamp;

    /**
     * Session 文件路径
     */
    @Field(type = FieldType.Text, name = "session_file")
    private String sessionFile;

    /**
     * 压缩次数
     */
    @Field(type = FieldType.Integer, name = "compaction_count")
    private Integer compactionCount;



    /**
     * 最后使用的渠道
     */
    @Field(type = FieldType.Keyword, name = "last_channel")
    private String lastChannel;

    /**
     * 聊天类型（direct/group/thread）
     */
    @Field(type = FieldType.Keyword, name = "chat_type")
    private String chatType;

    /**
     * Session Key
     */
    @Field(type = FieldType.Keyword, name = "session_key")
    private String sessionKey;

    /**
     * 工作区目录
     */
    @Field(type = FieldType.Text, name = "workspace_dir")
    private String workspaceDir;

    /**
     * Sandbox 模式
     */
    @Field(type = FieldType.Keyword, name = "sandbox_mode")
    private String sandboxMode;

    /**
     * 运行时长（毫秒）
     */
    @Field(type = FieldType.Long, name = "runtime_ms")
    private Long runtimeMs;

    // ==================== 来源信息（用于看板筛选）====================

    /**
     * Session 来源标签（UI 显示用）
     */
    @Field(type = FieldType.Text, name = "origin_label")
    private String originLabel;

    /**
     * 来源渠道（webchat/telegram/discord 等）
     */
    @Field(type = FieldType.Keyword, name = "origin_provider")
    private String originProvider;

    /**
     * 来源表面
     */
    @Field(type = FieldType.Keyword, name = "origin_surface")
    private String originSurface;

    /**
     * 聊天类型（direct/group/thread）
     */
    @Field(type = FieldType.Keyword, name = "origin_chat_type")
    private String originChatType;

    /**
     * 交付渠道
     */
    @Field(type = FieldType.Keyword, name = "delivery_channel")
    private String deliveryChannel;

    // ==================== 模型信息 ====================

    /**
     * 模型提供商
     */
    @Field(type = FieldType.Keyword, name = "model_provider")
    private String modelProvider;

    /**
     * 使用的模型
     */
    @Field(type = FieldType.Keyword, name = "model")
    private String model;

    /**
     * Skill 名称列表（空格分隔，从 systemPromptReport）
     */
    @Field(type = FieldType.Text, name = "skill_names")
    private String skillNames;

    /**
     * Tool 名称列表（空格分隔）
     */
    @Field(type = FieldType.Text, name = "tool_names")
    private String toolNames;

    /**
     * 注入的工作区文件名列表（空格分隔）
     */
    @Field(type = FieldType.Text, name = "injected_workspace_file_names")
    private String injectedWorkspaceFileNames;


}
