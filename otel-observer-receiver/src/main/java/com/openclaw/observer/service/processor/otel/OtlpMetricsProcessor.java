package com.openclaw.observer.service.processor.otel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.openclaw.observer.common.ProcessResult;
import com.openclaw.observer.document.OtelMetricDocument;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.repository.OtelMetricEsRepository;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.*;
import io.opentelemetry.proto.resource.v1.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处理 OTLP_METRICS 类型的 RawData
 */
@Slf4j
@Service
public class OtlpMetricsProcessor extends AbstractRawOtlpProcessor<ExportMetricsServiceRequest> {

    private final OtelMetricEsRepository metricEsRepository;

    public OtlpMetricsProcessor(OtelMetricEsRepository metricEsRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.metricEsRepository = metricEsRepository;
    }

    @Override
    protected ExportMetricsServiceRequest parseRequest(String rawJson) throws Exception {
        ExportMetricsServiceRequest.Builder builder = ExportMetricsServiceRequest.newBuilder();
        jsonParser.merge(rawJson, builder);
        return builder.build();
    }

    @Override
    protected ProcessResult doProcess(RawDataDocument rawDoc, ExportMetricsServiceRequest request) {
        ProcessResult result = new ProcessResult();
        result.metricIds = new ArrayList<>();

        for (ResourceMetrics resourceMetrics : request.getResourceMetricsList()) {
            Resource resource = resourceMetrics.getResource();
            Map<String, Object> resourceAttrs = attributesToMap(resource.getAttributesList());
            String serviceName = getServiceName(resourceAttrs);

            for (ScopeMetrics scopeMetrics : resourceMetrics.getScopeMetricsList()) {
                for (Metric metric : scopeMetrics.getMetricsList()) {
                    processMetricDataPoints(rawDoc, metric, serviceName, resourceAttrs, result);
                }
            }
        }

        return result;
    }

    @Override
    protected String getTypeLabel() {
        return "OTLP_METRICS";
    }

    @Override
    protected void logResult(ProcessResult result) {
        log.info("✅ OTLP Metrics 处理完成，解析 {} 条，存储 {} 条",
            result.parsedMetricCount, result.storedMetricCount);
    }

    private void processMetricDataPoints(RawDataDocument rawDoc, Metric metric, String serviceName,
                                          Map<String, Object> resourceAttrs, ProcessResult result) {
        if (metric.hasGauge()) {
            for (NumberDataPoint dp : metric.getGauge().getDataPointsList()) {
                saveMetricPoint(rawDoc, metric, "gauge", dp, null, null, serviceName, resourceAttrs, null, result);
            }
        }
        if (metric.hasSum()) {
            Sum sum = metric.getSum();
            String aggTemporality = sum.getAggregationTemporality().name();
            for (NumberDataPoint dp : sum.getDataPointsList()) {
                saveMetricPoint(rawDoc, metric, "sum", dp, null, null, serviceName, resourceAttrs, aggTemporality, result);
            }
        }
        if (metric.hasHistogram()) {
            Histogram histogram = metric.getHistogram();
            String aggTemporality = histogram.getAggregationTemporality().name();
            for (HistogramDataPoint dp : histogram.getDataPointsList()) {
                saveMetricPoint(rawDoc, metric, "histogram", null, dp, null, serviceName, resourceAttrs, aggTemporality, result);
            }
        }
        if (metric.hasSummary()) {
            for (SummaryDataPoint dp : metric.getSummary().getDataPointsList()) {
                saveMetricPoint(rawDoc, metric, "summary", null, null, dp, serviceName, resourceAttrs, null, result);
            }
        }
    }

    private void saveMetricPoint(RawDataDocument rawDoc, Metric metric, String metricType,
                                  NumberDataPoint numberDp,
                                  HistogramDataPoint histogramDp,
                                  SummaryDataPoint summaryDp,
                                  String serviceName,
                                  Map<String, Object> resourceAttrs,
                                  String aggregationTemporality,
                                  ProcessResult result) {

        OtelMetricDocument doc = new OtelMetricDocument();

        String docId = generateMetricId(serviceName, metric, metricType, numberDp, histogramDp, summaryDp);
        doc.setId(docId);
        doc.setCreatedAt(formatDate(java.time.LocalDateTime.now(SHANGHAI_ZONE)));
        doc.setRawDataId(rawDoc.getId());
        doc.setClientIp(rawDoc.getClientIp());

        doc.setMetricName(metric.getName());
        doc.setDescription(metric.getDescription());
        doc.setMetricType(metricType);
        doc.setServiceName(serviceName);
        doc.setAggregationTemporality(aggregationTemporality);

        Map<String, Object> pointAttrs = new java.util.HashMap<>();
        if (numberDp != null) {
            if (numberDp.hasAsDouble()) {
                doc.setValueDouble(numberDp.getAsDouble());
            } else if (numberDp.hasAsInt()) {
                doc.setValueLong(numberDp.getAsInt());
            }
            pointAttrs = attributesToMap(numberDp.getAttributesList());
            doc.setTimestamp(formatDate(nanosToLocalDateTime(numberDp.getTimeUnixNano())));
            if (numberDp.getStartTimeUnixNano() > 0) {
                doc.setStartTimestamp(formatDate(nanosToLocalDateTime(numberDp.getStartTimeUnixNano())));
            }
        } else if (histogramDp != null) {
            pointAttrs = attributesToMap(histogramDp.getAttributesList());
            doc.setTimestamp(formatDate(nanosToLocalDateTime(histogramDp.getTimeUnixNano())));
            if (histogramDp.getStartTimeUnixNano() > 0) {
                doc.setStartTimestamp(formatDate(nanosToLocalDateTime(histogramDp.getStartTimeUnixNano())));
            }
            doc.setHistogramCount((long) histogramDp.getCount());
            doc.setHistogramSum(histogramDp.getSum());
            if (histogramDp.hasMin()) {
                doc.setHistogramMin(histogramDp.getMin());
            }
            if (histogramDp.hasMax()) {
                doc.setHistogramMax(histogramDp.getMax());
            }
            doc.setHistogramBucketCounts(histogramDp.getBucketCountsList().stream().map(Long::valueOf).collect(Collectors.toList()));
            doc.setHistogramExplicitBounds(histogramDp.getExplicitBoundsList());
        } else if (summaryDp != null) {
            pointAttrs = attributesToMap(summaryDp.getAttributesList());
            doc.setTimestamp(formatDate(nanosToLocalDateTime(summaryDp.getTimeUnixNano())));
            if (summaryDp.getStartTimeUnixNano() > 0) {
                doc.setStartTimestamp(formatDate(nanosToLocalDateTime(summaryDp.getStartTimeUnixNano())));
            }
        }

        setResourceAttributes(doc, resourceAttrs);
        setPointAttributes(doc, pointAttrs);

        metricEsRepository.save(doc);
        result.addMetric(docId);
        log.debug("✅ Saved metric: name={}, type={}", doc.getMetricName(), doc.getMetricType());
    }

    private String generateMetricId(String serviceName, Metric metric, String metricType,
                                     NumberDataPoint numberDp, HistogramDataPoint histogramDp, SummaryDataPoint summaryDp) {
        StringBuilder sb = new StringBuilder();
        sb.append("metric-");
        sb.append(serviceName);
        sb.append("-");
        sb.append(metric.getName());
        sb.append("-");
        sb.append(metricType);

        if (numberDp != null) {
            sb.append("-");
            sb.append(numberDp.getTimeUnixNano());
            sb.append("-");
            sb.append(hashAttributes(numberDp.getAttributesList()));
        } else if (histogramDp != null) {
            sb.append("-");
            sb.append(histogramDp.getTimeUnixNano());
            sb.append("-");
            sb.append(hashAttributes(histogramDp.getAttributesList()));
        } else if (summaryDp != null) {
            sb.append("-");
            sb.append(summaryDp.getTimeUnixNano());
            sb.append("-");
            sb.append(hashAttributes(summaryDp.getAttributesList()));
        }

        return hash(sb.toString());
    }

    private void setResourceAttributes(OtelMetricDocument doc, Map<String, Object> resourceAttrs) {
        if (resourceAttrs == null) return;

        doc.setHostName(getStrAttr(resourceAttrs, "host.name"));
        doc.setHostArch(getStrAttr(resourceAttrs, "host.arch"));
        doc.setProcessPid(getLongAttr(resourceAttrs, "process.pid"));
        doc.setProcessExecutableName(getStrAttr(resourceAttrs, "process.executable.name"));
        doc.setProcessExecutablePath(getStrAttr(resourceAttrs, "process.executable.path"));
        doc.setProcessCommand(getStrAttr(resourceAttrs, "process.command"));
        doc.setProcessRuntimeName(getStrAttr(resourceAttrs, "process.runtime.name"));
        doc.setProcessRuntimeVersion(getStrAttr(resourceAttrs, "process.runtime.version"));
        doc.setProcessRuntimeDescription(getStrAttr(resourceAttrs, "process.runtime.description"));
        doc.setProcessOwner(getStrAttr(resourceAttrs, "process.owner"));

        Object argsObj = resourceAttrs.get("process.command_args");
        if (argsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> argsList = (List<String>) argsObj;
            doc.setProcessCommandArgs(String.join(" ", argsList));
        }
    }

    private void setPointAttributes(OtelMetricDocument doc, Map<String, Object> pointAttrs) {
        if (pointAttrs == null) return;

        doc.setOpenclawChannel(getStrAttr(pointAttrs, "openclaw.channel"));
        doc.setOpenclawSource(getStrAttr(pointAttrs, "openclaw.source"));
        doc.setOpenclawOutcome(getStrAttr(pointAttrs, "openclaw.outcome"));
        doc.setOpenclawLane(getStrAttr(pointAttrs, "openclaw.lane"));
        doc.setOpenclawState(getStrAttr(pointAttrs, "openclaw.state"));
        doc.setOpenclawReason(getStrAttr(pointAttrs, "openclaw.reason"));
    }
}
