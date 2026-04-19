package com.openclaw.observer.service.processor.otel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.openclaw.observer.common.ProcessResult;
import com.openclaw.observer.document.OtelTraceDocument;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.repository.OtelTraceEsRepository;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.resource.v1.Resource;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * 处理 OTLP_TRACES 类型的 RawData
 */
@Slf4j
@Service
public class OtlpTracesProcessor extends AbstractRawOtlpProcessor<ExportTraceServiceRequest> {

    private final OtelTraceEsRepository traceEsRepository;

    public OtlpTracesProcessor(OtelTraceEsRepository traceEsRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.traceEsRepository = traceEsRepository;
    }

    @Override
    protected ExportTraceServiceRequest parseRequest(String rawJson) throws Exception {
        ExportTraceServiceRequest.Builder builder = ExportTraceServiceRequest.newBuilder();
        jsonParser.merge(rawJson, builder);
        return builder.build();
    }

    @Override
    protected ProcessResult doProcess(RawDataDocument rawDoc, ExportTraceServiceRequest request) {
        ProcessResult result = new ProcessResult();
        result.traceIds = new ArrayList<>();

        for (ResourceSpans resourceSpans : request.getResourceSpansList()) {
            Resource resource = resourceSpans.getResource();
            Map<String, Object> resourceAttrs = attributesToMap(resource.getAttributesList());
            String serviceName = getServiceName(resourceAttrs);

            for (ScopeSpans scopeSpans : resourceSpans.getScopeSpansList()) {
                for (Span span : scopeSpans.getSpansList()) {
                    OtelTraceDocument doc = buildTraceDocument(rawDoc, serviceName, resourceAttrs, scopeSpans, span);
                    traceEsRepository.save(doc);
                    result.addTrace(doc.getId());
                    log.info("✅ Saved span: traceId={}, spanId={}", doc.getTraceId(), doc.getSpanId());
                }
            }
        }

        return result;
    }

    @Override
    protected String getTypeLabel() {
        return "OTLP_TRACES";
    }

    @Override
    protected void logResult(ProcessResult result) {
        log.info("✅ OTLP Traces 处理完成，解析 {} 条，存储 {} 条",
            result.parsedTraceCount, result.storedTraceCount);
    }

    private OtelTraceDocument buildTraceDocument(RawDataDocument rawDoc, String serviceName,
                                                   Map<String, Object> resourceAttrs,
                                                   ScopeSpans scopeSpans, Span span) {
        OtelTraceDocument doc = new OtelTraceDocument();

        doc.setRawDataId(rawDoc.getId());
        String docId = generateTraceId(span);
        doc.setId(docId);
        doc.setCreatedAt(formatDate(LocalDateTime.now(SHANGHAI_ZONE)));
        doc.setClientIp(rawDoc.getClientIp());

        doc.setTraceId(bytesToHex(span.getTraceId().toByteArray()));
        doc.setSpanId(bytesToHex(span.getSpanId().toByteArray()));
        doc.setParentSpanId(bytesToHex(span.getParentSpanId().toByteArray()));
        doc.setServiceName(serviceName);
        doc.setSpanName(span.getName());
        doc.setSpanKind(span.getKind().name());
        doc.setStartTime(formatDate(nanosToLocalDateTime(span.getStartTimeUnixNano())));
        doc.setEndTime(formatDate(nanosToLocalDateTime(span.getEndTimeUnixNano())));
        doc.setDurationMs((span.getEndTimeUnixNano() - span.getStartTimeUnixNano()) / 1_000_000);

        if (span.hasStatus()) {
            doc.setStatusCode(span.getStatus().getCode().name());
            doc.setStatusDescription(span.getStatus().getMessage());
        }

        try {
            doc.setEvents(objectMapper.writeValueAsString(span.getEventsList()));
        } catch (Exception e) {
            doc.setEvents("[]");
        }
        try {
            doc.setLinks(objectMapper.writeValueAsString(span.getLinksList()));
        } catch (Exception e) {
            doc.setLinks("[]");
        }

        // Scope 信息
        if (scopeSpans.hasScope()) {
            doc.setScopeName(scopeSpans.getScope().getName());
            doc.setScopeVersion(scopeSpans.getScope().getVersion());
        }

        // Resource attributes
        doc.setHostName(getStrAttr(resourceAttrs, "host.name"));
        doc.setHostArch(getStrAttr(resourceAttrs, "host.arch"));
        doc.setProcessPid(getLongAttr(resourceAttrs, "process.pid"));
        doc.setProcessExecutableName(getStrAttr(resourceAttrs, "process.executable.name"));
        doc.setProcessRuntimeVersion(getStrAttr(resourceAttrs, "process.runtime.version"));
        doc.setProcessRuntimeName(getStrAttr(resourceAttrs, "process.runtime.name"));
        doc.setProcessOwner(getStrAttr(resourceAttrs, "process.owner"));

        // Span attributes (OpenClaw 特有)
        Map<String, Object> spanAttrs = attributesToMap(span.getAttributesList());
        doc.setOpenclawChannel(getStrAttr(spanAttrs, "openclaw.channel"));
        doc.setOpenclawOutcome(getStrAttr(spanAttrs, "openclaw.outcome"));
        doc.setOpenclawSessionKey(getStrAttr(spanAttrs, "openclaw.sessionKey"));
        doc.setOpenclawMessageId(getStrAttr(spanAttrs, "openclaw.messageId"));

        // Trace state
        String traceState = span.getTraceState();
        if (traceState != null && !traceState.isEmpty()) {
            doc.setTraceState(traceState);
        }

        return doc;
    }

    private String generateTraceId(Span span) {
        StringBuilder sb = new StringBuilder();
        sb.append("trace-");
        sb.append(bytesToHex(span.getTraceId().toByteArray()));
        sb.append("-");
        sb.append(bytesToHex(span.getSpanId().toByteArray()));
        return hash(sb.toString());
    }
}
