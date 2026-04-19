package com.openclaw.observer.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "otel-metrics")
public class OtelMetricDocument {

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

    // === 指标基础信息 ===

    /**
     * 指标名称（如 "openclaw.message.duration_ms"）
     * 来源：Metric.name
     */
    @Field(type = FieldType.Keyword, name = "metric_name")
    private String metricName;

    /**
     * 指标描述（如 "Message processing duration"）
     * 来源：Metric.description
     */
    @Field(type = FieldType.Text, name = "description")
    private String description;

    /**
     * 指标类型（gauge/sum/histogram/summary）
     * 来源：根据 Metric 哪个字段被设置推断
     */
    @Field(type = FieldType.Keyword, name = "metric_type")
    private String metricType;

    /**
     * 服务名称（通常是 "openclaw"）
     * 来源：Resource.attributes["service.name"]
     */
    @Field(type = FieldType.Keyword, name = "service_name")
    private String serviceName;

    /**
     * 聚合时序类型（CUMULATIVE/DELTA）
     * 来源：Sum.aggregation_temporality 或 Histogram.aggregation_temporality
     */
    @Field(type = FieldType.Keyword, name = "aggregation_temporality")
    private String aggregationTemporality;

    // === 时间字段 - 指标采集时间（不是接收时间！）===

    /**
     * 指标采集时间戳（DataPoint 产生时间）
     * 来源：NumberDataPoint.time_unix_nano / HistogramDataPoint.time_unix_nano
     */
    @Field(type = FieldType.Date, name = "timestamp")
    private String timestamp;

    /**
     * 累积开始时间戳（用于 CUMULATIVE 类型指标）
     * 来源：NumberDataPoint.start_time_unix_nano / HistogramDataPoint.start_time_unix_nano
     */
    @Field(type = FieldType.Date, name = "start_timestamp")
    private String startTimestamp;

    // === 数值字段（Sum/Gauge 用）===

    /**
     * 双精度浮点数值（Sum/Gauge 类型用）
     * 来源：NumberDataPoint.as_double
     */
    @Field(type = FieldType.Double, name = "value_double")
    private Double valueDouble;

    /**
     * 长整型数值（Sum/Gauge 类型用）
     * 来源：NumberDataPoint.as_int
     */
    @Field(type = FieldType.Long, name = "value_long")
    private Long valueLong;

    /**
     * 整型数值（保留字段，暂未使用）
     */
    @Field(type = FieldType.Integer, name = "value_int")
    private Integer valueInt;

    // === Resource Attributes（通用 - 打平到顶层）===

    /**
     * 主机名
     * 来源：Resource.attributes["host.name"]
     */
    @Field(type = FieldType.Keyword, name = "host_name")
    private String hostName;

    /**
     * CPU 架构（arm64/x64）
     * 来源：Resource.attributes["host.arch"]
     */
    @Field(type = FieldType.Keyword, name = "host_arch")
    private String hostArch;

    /**
     * 进程 ID
     * 来源：Resource.attributes["process.pid"]
     */
    @Field(type = FieldType.Long, name = "process_pid")
    private Long processPid;

    /**
     * 可执行文件名称
     * 来源：Resource.attributes["process.executable.name"]
     */
    @Field(type = FieldType.Keyword, name = "process_executable_name")
    private String processExecutableName;

    /**
     * 可执行文件完整路径
     * 来源：Resource.attributes["process.executable.path"]
     */
    @Field(type = FieldType.Keyword, name = "process_executable_path")
    private String processExecutablePath;

    /**
     * 启动命令
     * 来源：Resource.attributes["process.command"]
     */
    @Field(type = FieldType.Keyword, name = "process_command")
    private String processCommand;

    /**
     * 运行时名称（nodejs）
     * 来源：Resource.attributes["process.runtime.name"]
     */
    @Field(type = FieldType.Keyword, name = "process_runtime_name")
    private String processRuntimeName;

    /**
     * 运行时版本
     * 来源：Resource.attributes["process.runtime.version"]
     */
    @Field(type = FieldType.Keyword, name = "process_runtime_version")
    private String processRuntimeVersion;

    /**
     * 运行时描述（Node.js）
     * 来源：Resource.attributes["process.runtime.description"]
     */
    @Field(type = FieldType.Keyword, name = "process_runtime_description")
    private String processRuntimeDescription;

    /**
     * 进程所有者用户名
     * 来源：Resource.attributes["process.owner"]
     */
    @Field(type = FieldType.Keyword, name = "process_owner")
    private String processOwner;

    /**
     * 命令行参数（逗号分隔字符串）
     * 来源：Resource.attributes["process.command_args"]
     */
    @Field(type = FieldType.Text, name = "process_command_args")
    private String processCommandArgs;

    // === Point Attributes（OpenClaw 特定 - 打平到顶层）===

    /**
     * 消息渠道（webchat/telegram/discord 等）
     * 来源：DataPoint.attributes["openclaw.channel"]
     */
    @Field(type = FieldType.Keyword, name = "openclaw_channel")
    private String openclawChannel;

    /**
     * 消息来源（dispatch 等）
     * 来源：DataPoint.attributes["openclaw.source"]
     */
    @Field(type = FieldType.Keyword, name = "openclaw_source")
    private String openclawSource;

    /**
     * 处理结果（completed/failed 等）
     * 来源：DataPoint.attributes["openclaw.outcome"]
     */
    @Field(type = FieldType.Keyword, name = "openclaw_outcome")
    private String openclawOutcome;

    /**
     * 队列 Lane（main/session:agent:xxx 等）
     * 来源：DataPoint.attributes["openclaw.lane"]
     */
    @Field(type = FieldType.Keyword, name = "openclaw_lane")
    private String openclawLane;

    /**
     * Session 状态（idle/processing）
     * 来源：DataPoint.attributes["openclaw.state"]
     */
    @Field(type = FieldType.Keyword, name = "openclaw_state")
    private String openclawState;

    /**
     * 状态变更原因（message_start/run_completed 等）
     * 来源：DataPoint.attributes["openclaw.reason"]
     */
    @Field(type = FieldType.Keyword, name = "openclaw_reason")
    private String openclawReason;

    // === Histogram 字段（全部打平到顶层，不用 Object！）===

    /**
     * Histogram 总样本数
     * 来源：HistogramDataPoint.count
     */
    @Field(type = FieldType.Long, name = "histogram_count")
    private Long histogramCount;

    /**
     * Histogram 所有值的总和
     * 来源：HistogramDataPoint.sum
     */
    @Field(type = FieldType.Double, name = "histogram_sum")
    private Double histogramSum;

    /**
     * Histogram 最小值
     * 来源：HistogramDataPoint.min
     */
    @Field(type = FieldType.Double, name = "histogram_min")
    private Double histogramMin;

    /**
     * Histogram 最大值
     * 来源：HistogramDataPoint.max
     */
    @Field(type = FieldType.Double, name = "histogram_max")
    private Double histogramMax;

    /**
     * Histogram 每个桶的计数数组
     * 来源：HistogramDataPoint.bucket_counts
     */
    @Field(type = FieldType.Long, name = "histogram_bucket_counts")
    private List<Long> histogramBucketCounts;

    /**
     * Histogram 桶边界数组
     * 来源：HistogramDataPoint.explicit_bounds
     */
    @Field(type = FieldType.Double, name = "histogram_explicit_bounds")
    private List<Double> histogramExplicitBounds;

    /**
     * 客户端 IP（来自 RawDataDocument）
     */
    @Field(type = FieldType.Keyword, name = "client_ip")
    private String clientIp;

}
