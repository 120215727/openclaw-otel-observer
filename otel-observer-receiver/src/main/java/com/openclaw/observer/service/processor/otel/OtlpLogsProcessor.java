package com.openclaw.observer.service.processor.otel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import com.openclaw.observer.common.ProcessResult;
import com.openclaw.observer.document.OtelLogDocument;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.repository.OtelLogEsRepository;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.resource.v1.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * 处理 OTLP_LOGS 类型的 RawData
 */
@Slf4j
@Service
public class OtlpLogsProcessor extends AbstractRawOtlpProcessor<ExportLogsServiceRequest> {

    private final OtelLogEsRepository logEsRepository;

    public OtlpLogsProcessor(OtelLogEsRepository logEsRepository, ObjectMapper objectMapper) {
        super(objectMapper);
        this.logEsRepository = logEsRepository;
    }

    @Override
    protected ExportLogsServiceRequest parseRequest(String rawJson) throws Exception {
        ExportLogsServiceRequest.Builder builder = ExportLogsServiceRequest.newBuilder();
        jsonParser.merge(rawJson, builder);
        return builder.build();
    }

    @Override
    protected ProcessResult doProcess(RawDataDocument rawDoc, ExportLogsServiceRequest request) {
        ProcessResult result = new ProcessResult();
        result.logIds = new ArrayList<>();

        for (ResourceLogs resourceLogs : request.getResourceLogsList()) {
            Resource resource = resourceLogs.getResource();
            Map<String, Object> resourceAttrs = attributesToMap(resource.getAttributesList());
            String serviceName = getServiceName(resourceAttrs);

            for (ScopeLogs scopeLogs : resourceLogs.getScopeLogsList()) {
                for (LogRecord logRecord : scopeLogs.getLogRecordsList()) {
                    OtelLogDocument doc = buildLogDocument(rawDoc, serviceName, logRecord);
                    logEsRepository.save(doc);
                    result.addLog(doc.getId());
                    log.info("✅ Saved log: service={}, level={}, location={}",
                        doc.getServiceName(), doc.getLogLevel(), doc.getOpenclawCodeLocation());
                }
            }
        }

        return result;
    }

    @Override
    protected String getTypeLabel() {
        return "OTLP_LOGS";
    }

    @Override
    protected void logResult(ProcessResult result) {
        log.info("✅ OTLP Logs 处理完成，解析 {} 条，存储 {} 条",
            result.parsedLogCount, result.storedLogCount);
    }

    private OtelLogDocument buildLogDocument(RawDataDocument rawDoc, String serviceName, LogRecord logRecord) {
        OtelLogDocument doc = new OtelLogDocument();

        doc.setRawDataId(rawDoc.getId());
        String docId = generateLogId(serviceName, logRecord);
        doc.setId(docId);
        doc.setCreatedAt(formatDate(LocalDateTime.now(SHANGHAI_ZONE)));
        doc.setClientIp(rawDoc.getClientIp());

        doc.setTraceId(bytesToHex(logRecord.getTraceId().toByteArray()));
        doc.setSpanId(bytesToHex(logRecord.getSpanId().toByteArray()));
        doc.setServiceName(serviceName);
        doc.setLogLevel(logRecord.getSeverityText());

        if (logRecord.hasBody()) {
            doc.setMessage(anyValueToString(logRecord.getBody()));
        }

        if (logRecord.getTimeUnixNano() > 0) {
            doc.setTimestamp(formatDate(nanosToLocalDateTime(logRecord.getTimeUnixNano())));
        }
        if (logRecord.getObservedTimeUnixNano() > 0) {
            doc.setObservedTimestamp(formatDate(nanosToLocalDateTime(logRecord.getObservedTimeUnixNano())));
        }

        Map<String, Object> attrs = attributesToMap(logRecord.getAttributesList());
        doc.setOpenclawSubsystem(getStrAttr(attrs, "openclaw.subsystem"));
        doc.setOpenclawCodeLocation(getStrAttr(attrs, "openclaw.code.location"));
        doc.setCodeFilepath(getStrAttr(attrs, "code.filepath"));
        doc.setCodeFunction(getStrAttr(attrs, "code.function"));

        Object lineno = attrs.get("code.lineno");
        if (lineno instanceof Number) {
            doc.setCodeLineno(((Number) lineno).intValue());
        } else if (lineno instanceof String) {
            try {
                doc.setCodeLineno(Integer.parseInt((String) lineno));
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return doc;
    }

    private String generateLogId(String serviceName, LogRecord logRecord) {
        StringBuilder sb = new StringBuilder();
        sb.append("log-");
        sb.append(serviceName);
        sb.append("-");

        String traceId = bytesToHex(logRecord.getTraceId().toByteArray());
        if (traceId != null && !traceId.isEmpty()) {
            sb.append(traceId);
            sb.append("-");
        }

        sb.append(logRecord.getTimeUnixNano());
        sb.append("-");
        sb.append(hashAttributes(logRecord.getAttributesList()));

        if (logRecord.hasBody()) {
            sb.append("-");
            sb.append(anyValueToString(logRecord.getBody()).hashCode());
        }

        return hash(sb.toString());
    }
}
