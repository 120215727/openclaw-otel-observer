package com.openclaw.observer.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;



/**
 * OTel Logs Elasticsearch Document
 * 
 * 基于 OpenTelemetry Logs Protocol 和 OpenClaw 实际数据设计
 * 核心字段优先级：
 * 🔥 1. message         - 日志内容（最核心）
 * 🔥 2. timestamp       - 日志发生时间
 * 🔥 3. log_level       - 日志级别（ERROR/WARN/INFO/DEBUG/TRACE）
 * 🔥 4. openclaw_subsystem - 子系统（gateway/ws 等）
 * 🔥 5. trace_id/span_id - 链路追踪关联
 * 🔥 6. code_filepath + code_lineno - 代码位置
 * 
 * 参考：
 * - https://github.com/open-telemetry/opentelemetry-proto/blob/main/opentelemetry/proto/logs/v1/logs.proto
 * - docs/samples/log.json
 */
@Data
@Document(indexName = "otel-logs")
public class OtelLogDocument {

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

    // ==================== 时间字段（核心）====================

    /**
     * 日志发生时间（LogRecord.time_unix_nano）
     * 🔥 核心字段 - 用于时间轴排序、范围查询
     */
    @Field(type = FieldType.Date, name = "timestamp")
    private String timestamp;

    /**
     * 日志采集时间（LogRecord.observed_time_unix_nano）
     * 用于排查数据延迟问题
     */
    @Field(type = FieldType.Date, name = "observed_timestamp")
    private String observedTimestamp;

    // ==================== 基础信息（核心）====================

    /**
     * 服务名（Resource.attributes["service.name"]）
     * 多服务场景下用于筛选
     */
    @Field(type = FieldType.Keyword, name = "service_name")
    private String serviceName;

    /**
     * 日志级别（LogRecord.severity_text）
     * 🔥 核心字段 - ERROR/WARN/INFO/DEBUG/TRACE
     * 用于错误告警、日志级别分布统计
     */
    @Field(type = FieldType.Keyword, name = "log_level")
    private String logLevel;


    /**
     * 日志消息内容（LogRecord.body.string_value）
     * 🔥 最核心字段 - 实际日志文本
     * 用于全文搜索、关键词过滤
     */
    @Field(type = FieldType.Text, name = "message")
    private String message;

    // ==================== 链路追踪（核心）====================

    /**
     * Trace ID（LogRecord.trace_id）
     * 🔥 核心字段 - 关联到 Trace 数据
     * 用于链路追踪、跨服务调用链分析
     */
    @Field(type = FieldType.Keyword, name = "trace_id")
    private String traceId;

    /**
     * Span ID（LogRecord.span_id）
     * 🔥 核心字段 - 关联到具体 Span
     * 用于定位到具体的调用跨度
     */
    @Field(type = FieldType.Keyword, name = "span_id")
    private String spanId;

    // ==================== OpenClaw 特有字段 ====================

    /**
     * OpenClaw 子系统（attributes["openclaw.subsystem"]）
     * 如 gateway/ws, agent/embedded 等
     */
    @Field(type = FieldType.Keyword, name = "openclaw_subsystem")
    private String openclawSubsystem;

    /**
     * OpenClaw 代码位置（attributes["openclaw.code.location"]）
     * 🔥 核心字段 - 直接指向日志产生的代码位置
     */
    @Field(type = FieldType.Keyword, name = "openclaw_code_location")
    private String openclawCodeLocation;

    /**
     * 代码文件路径（attributes["code.filepath"]）
     */
    @Field(type = FieldType.Keyword, name = "code_filepath")
    private String codeFilepath;

    /**
     * 代码行号（attributes["code.lineno"]）
     */
    @Field(type = FieldType.Integer, name = "code_lineno")
    private Integer codeLineno;

    /**
     * 代码函数名（attributes["code.function"]）
     */
    @Field(type = FieldType.Keyword, name = "code_function")
    private String codeFunction;

    /**
     * 客户端 IP（来自 RawDataDocument）
     */
    @Field(type = FieldType.Keyword, name = "client_ip")
    private String clientIp;

}
