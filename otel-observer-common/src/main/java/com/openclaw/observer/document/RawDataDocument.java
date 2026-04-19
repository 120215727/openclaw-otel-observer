package com.openclaw.observer.document;

import com.openclaw.observer.common.enums.ProcessingStatus;
import com.openclaw.observer.common.enums.RawType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Raw Data Document - 原始数据存储（解耦层）
 * <p>
 * 设计目标：
 * - 解耦原始请求数据和 Dashboard 数据
 * - 可追溯：知道每个请求处理了多少数据
 * - 可重放：可以从 Raw Data 重新生成 Dashboard 数据
 * - 一致性保证：更新时先删后加
 * <p>
 * 索引：oc-raw-data
 * 粒度：1 条 / 请求（或 1 条 / Session JSONL 文件）
 */
@Data
@Document(indexName = "oc-raw-data")
public class RawDataDocument {

    @Id
    private String id;

    // ==================== 基础信息 ====================

    /**
     * Raw Data 类型
     * otlp_logs / otlp_metrics / otlp_traces / session_jsonl / session_upload
     */
    @Field(type = FieldType.Keyword, name = "raw_type")
    private RawType rawType;

    /**
     * 客户端 IP
     */
    @Field(type = FieldType.Ip, name = "client_ip")
    private String clientIp;

    // ==================== 原始数据存储 ====================

    /**
     * 原始数据（JSON 字符串）
     * - OTLP 请求：完整的 OTLP JSON
     * - Session 文件：完整的 JSONL 内容（或分块存储）
     */
    @Field(type = FieldType.Text, name = "raw_data")
    private String rawData;

    /**
     * 原始数据格式版本
     */
    @Field(type = FieldType.Keyword, name = "raw_version")
    private String rawDataVersion = "v1";

    // ==================== 处理状态 ====================

    /**
     * 处理状态
     * pending / processing / success / failed / partially_failed
     */
    @Field(type = FieldType.Keyword, name = "processing_status")
    private ProcessingStatus processingStatus;

    /**
     * 处理开始时间
     */
    @Field(type = FieldType.Date, name = "processing_started_at")
    private String processingStartedAt;

    /**
     * 处理结束时间
     */
    @Field(type = FieldType.Date, name = "processing_completed_at")
    private String processingCompletedAt;

    /**
     * 错误信息（如果 processing_status = failed）
     */
    @Field(type = FieldType.Text, name = "error_message")
    private String errorMessage;

    /**
     * 错误堆栈（如果 processing_status = failed）
     */
    @Field(type = FieldType.Text, name = "error_stack_trace")
    private String errorStackTrace;


    // ==================== 关联信息（方便查找）====================

    /**
     * 关联的 Agent ID（如果有）
     */
    @Field(type = FieldType.Keyword, name = "agent_id")
    private String agentId;

    /**
     * 关联的 Session ID（如果有）
     */
    @Field(type = FieldType.Keyword, name = "session_id")
    private String sessionId;

    /**
     * 关联的 Service Name（如果是 OTLP 数据）
     */
    @Field(type = FieldType.Keyword, name = "service_name")
    private String serviceName;

    // ==================== 元数据 ====================

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, name = "created_at")
    private String createdAt;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, name = "updated_at")
    private String updatedAt;


    // ==================== 便捷方法 ====================

    /**
     * 是否处理成功
     */
    public boolean isSuccess() {
        return ProcessingStatus.SUCCESS.equals(processingStatus);
    }

    /**
     * 是否处理失败
     */
    public boolean isFailed() {
        return ProcessingStatus.FAILED.equals(processingStatus);
    }

    /**
     * 是否部分失败
     */
    public boolean isPartiallyFailed() {
        return ProcessingStatus.PARTIALLY_FAILED.equals(processingStatus);
    }

}
