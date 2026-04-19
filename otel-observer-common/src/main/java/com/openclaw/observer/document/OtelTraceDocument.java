package com.openclaw.observer.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * OTel Traces Elasticsearch Document
 * 
 * 基于 OpenTelemetry Trace Protocol 和 OpenClaw 实际数据设计
 * 核心字段优先级：
 * 🔥 1. trace_id         - Trace ID（整条链路唯一标识）
 * 🔥 2. span_id          - Span ID（当前 Span 唯一标识）
 * 🔥 3. parent_span_id   - 父 Span ID（构建调用树）
 * 🔥 4. span_name        - Span 名称（操作名称）
 * 🔥 5. start_time/end_time - 时间范围
 * 🔥 6. duration_ms      - 耗时（性能分析核心）
 * 
 * 参考：
 * - https://github.com/open-telemetry/opentelemetry-proto/blob/main/opentelemetry/proto/trace/v1/trace.proto
 * - docs/samples/traces.json
 */
@Data
@Document(indexName = "otel-traces")
public class OtelTraceDocument {

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

    // ==================== 链路标识（核心）====================

    /**
     * Trace ID（Span.trace_id）
     * 🔥 核心字段 - 整条链路的唯一标识
     * 用于关联同一 Trace 下的所有 Span，构建完整调用链
     */
    @Field(type = FieldType.Keyword, name = "trace_id")
    private String traceId;

    /**
     * Span ID（Span.span_id）
     * 🔥 核心字段 - 当前 Span 的唯一标识
     */
    @Field(type = FieldType.Keyword, name = "span_id")
    private String spanId;

    /**
     * 父 Span ID（Span.parent_span_id）
     * 🔥 核心字段 - 构建 Span 树状结构
     * 为空表示是根 Span
     */
    @Field(type = FieldType.Keyword, name = "parent_span_id")
    private String parentSpanId;

    // ==================== 基础信息（核心）====================

    /**
     * 服务名（Resource.attributes["service.name"]）
     * 多服务场景下用于筛选
     */
    @Field(type = FieldType.Keyword, name = "service_name")
    private String serviceName;

    /**
     * Span 名称（Span.name）
     * 🔥 核心字段 - 操作名称，如 "openclaw.message.processed"
     * 用于 Span 分类统计、筛选
     */
    @Field(type = FieldType.Text, name = "span_name")
    private String spanName;

    /**
     * Span 类型（Span.kind）
     * SPAN_KIND_INTERNAL / SPAN_KIND_SERVER / SPAN_KIND_CLIENT /
     * SPAN_KIND_PRODUCER / SPAN_KIND_CONSUMER
     */
    @Field(type = FieldType.Keyword, name = "span_kind")
    private String spanKind;

    // ==================== 时间与性能（核心）====================

    /**
     * 开始时间（Span.start_time_unix_nano）
     * 🔥 核心字段 - Span 开始执行时间
     */
    @Field(type = FieldType.Date, name = "start_time")
    private String startTime;

    /**
     * 结束时间（Span.end_time_unix_nano）
     * 🔥 核心字段 - Span 结束执行时间
     */
    @Field(type = FieldType.Date, name = "end_time")
    private String endTime;

    /**
     * 耗时（毫秒）
     * 🔥 核心字段 - end_time - start_time
     * 用于性能分析、慢查询告警、P50/P95/P99 统计
     */
    @Field(type = FieldType.Long, name = "duration_ms")
    private Long durationMs;

    // ==================== 状态（核心）====================

    /**
     * 状态码（Span.status.code）
     * 🔥 核心字段 - STATUS_CODE_OK / STATUS_CODE_ERROR / STATUS_CODE_UNSET
     * 用于错误告警、成功率统计
     */
    @Field(type = FieldType.Keyword, name = "status_code")
    private String statusCode;

    /**
     * 状态描述（Span.status.message）
     * 错误时的详细描述
     */
    @Field(type = FieldType.Text, name = "status_description")
    private String statusDescription;

    // ==================== OpenClaw 特有 Attributes（打平）====================

    /**
     * 渠道（attributes["openclaw.channel"]）
     * 如 "webchat"、"telegram" 等
     */
    @Field(type = FieldType.Keyword, name = "openclaw_channel")
    private String openclawChannel;

    /**
     * 结果（attributes["openclaw.outcome"]）
     * 如 "completed"、"error" 等
     */
    @Field(type = FieldType.Keyword, name = "openclaw_outcome")
    private String openclawOutcome;

    /**
     * Session Key（attributes["openclaw.sessionKey"]）
     * 关联到 Session 数据
     */
    @Field(type = FieldType.Keyword, name = "openclaw_session_key")
    private String openclawSessionKey;

    /**
     * Message ID（attributes["openclaw.messageId"]）
     * 关联到 Message 数据
     */
    @Field(type = FieldType.Keyword, name = "openclaw_message_id")
    private String openclawMessageId;

    // ==================== Scope 信息====================

    /**
     * Scope 名称（Scope.name）
     * 通常为 "openclaw"
     */
    @Field(type = FieldType.Keyword, name = "scope_name")
    private String scopeName;

    /**
     * Scope 版本（Scope.version）
     */
    @Field(type = FieldType.Keyword, name = "scope_version")
    private String scopeVersion;

    // ==================== Resource 信息（打平）====================

    /**
     * 主机名（Resource.attributes["host.name"]）
     */
    @Field(type = FieldType.Keyword, name = "host_name")
    private String hostName;

    /**
     * CPU 架构（Resource.attributes["host.arch"]）
     * 如 "arm64"、"amd64"
     */
    @Field(type = FieldType.Keyword, name = "host_arch")
    private String hostArch;

    /**
     * 进程 ID（Resource.attributes["process.pid"]）
     */
    @Field(type = FieldType.Long, name = "process_pid")
    private Long processPid;

    /**
     * 进程可执行文件名（Resource.attributes["process.executable.name"]）
     */
    @Field(type = FieldType.Keyword, name = "process_executable_name")
    private String processExecutableName;

    /**
     * 运行时版本（Resource.attributes["process.runtime.version"]）
     * 如 "22.22.1"
     */
    @Field(type = FieldType.Keyword, name = "process_runtime_version")
    private String processRuntimeVersion;

    /**
     * 运行时名称（Resource.attributes["process.runtime.name"]）
     * 如 "nodejs"
     */
    @Field(type = FieldType.Keyword, name = "process_runtime_name")
    private String processRuntimeName;

    /**
     * 进程所有者（Resource.attributes["process.owner"]）
     */
    @Field(type = FieldType.Keyword, name = "process_owner")
    private String processOwner;

    // ==================== 兼容与扩展====================

    /**
     * Trace State（Span.trace_state）
     * W3C Trace Context - trace_state
     */
    @Field(type = FieldType.Text, name = "trace_state")
    private String traceState;

    /**
     * Events JSON 字符串（Span.events）
     * Span 生命周期内的事件列表
     */
    @Field(type = FieldType.Text, name = "events")
    private String events;

    /**
     * Links JSON 字符串（Span.links）
     * 关联到其他 Span 的链接
     */
    @Field(type = FieldType.Text, name = "links")
    private String links;

    /**
     * 客户端 IP（来自 RawDataDocument）
     */
    @Field(type = FieldType.Keyword, name = "client_ip")
    private String clientIp;

}
