package com.openclaw.observer.controller;

import com.openclaw.observer.common.util.PageableHelper;
import com.openclaw.observer.document.*;
import com.openclaw.observer.dto.*;
import com.openclaw.observer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ApiController {

    private final OtelTraceEsRepository otelTraceEsRepository;
    private final OtelMetricEsRepository otelMetricEsRepository;
    private final OtelLogEsRepository otelLogEsRepository;
    private final SessionEsRepository sessionEsRepository;
    private final SessionEventEsRepository sessionEventEsRepository;

    // ==================== Stats ====================

    @GetMapping("/stats")
    public StatsResponse getStats() {
        log.info("API Call: /api/stats");

        List<String> services = otelTraceEsRepository.findDistinctServiceNames();

        StatsResponse stats = StatsResponse.builder()
                .traceCount(otelTraceEsRepository.count())
                .metricCount(otelMetricEsRepository.count())
                .logCount(otelLogEsRepository.count())
                .sessionCount(sessionEsRepository.count())
                .services(services)
                .build();

        log.info("API /api/stats success");
        return stats;
    }

    @GetMapping("/stats/dashboard")
    public DashboardStatsResponse getDashboardStats() {
        log.info("API Call: /api/stats/dashboard");

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .traceCount(otelTraceEsRepository.count())
                .metricCount(otelMetricEsRepository.count())
                .logCount(otelLogEsRepository.count())
                .sessionCount(sessionEsRepository.count())
                .logLevels(getLogLevelDistribution())
                .recentActivity(getRecentActivity())
                .topMetrics(getTopMetrics())
                .build();

        log.info("API /api/stats/dashboard success");
        return stats;
    }

    private LogLevelDistributionResponse getLogLevelDistribution() {
        Map<String, Long> counts = otelLogEsRepository.getLogLevelCounts();
        return LogLevelDistributionResponse.builder()
                .info(counts.getOrDefault("INFO", 0L))
                .warn(counts.getOrDefault("WARN", 0L))
                .error(counts.getOrDefault("ERROR", 0L))
                .debug(counts.getOrDefault("DEBUG", 0L))
                .build();
    }

    private RecentActivityResponse getRecentActivity() {
        return RecentActivityResponse.builder()
                .traces24h(0L)
                .logs24h(0L)
                .metrics24h(0L)
                .build();
    }

    private List<MetricCountResponse> getTopMetrics() {
        Map<String, Long> metricCounts = otelMetricEsRepository.getTopMetricCounts(10);
        return metricCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(e -> MetricCountResponse.builder()
                        .name(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== Sessions ====================

    @GetMapping("/sessions")
    public PageResponse<SessionResponse> getSessions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "agentId", required = false) String agentId) {
        log.info("API Call: /api/sessions, page={}, size={}", page, size);

        Pageable pageable = PageableHelper.createDescByCreatedAt(page, size);
        Page<SessionDocument> sessionPage = sessionEsRepository.findAll(pageable);
        List<SessionResponse> sessionList = PageableHelper.convertPageToList(sessionPage, SessionResponse::from);

        PageResponse<SessionResponse> result = PageResponse.of(
                sessionList, sessionPage.getTotalElements(), page, size);

        log.info("API /api/sessions success, items={}, total={}", sessionList.size(), sessionPage.getTotalElements());
        return result;
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable String sessionId) {
        log.info("API Call: /api/sessions/{}", sessionId);
        Optional<SessionDocument> session = sessionEsRepository.findBySessionId(sessionId);
        if (session.isPresent()) {
            log.info("API /api/sessions/{} success", sessionId);
            return ResponseEntity.ok(SessionResponse.from(session.get()));
        } else {
            log.warn("API /api/sessions/{} not found", sessionId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/sessions/{sessionId}/events")
    public PageResponse<SessionEventResponse> getSessionEvents(
            @PathVariable String sessionId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size) {
        log.info("API Call: /api/sessions/{}/events, page={}, size={}", sessionId, page, size);

        Pageable pageable = PageableHelper.createAscByCreatedAt(page, size);
        List<SessionEventResponse> events = StreamSupport.stream(sessionEventEsRepository.findAll(pageable).spliterator(), false)
                .filter(e -> sessionId.equals(e.getSessionId()))
                .map(SessionEventResponse::from)
                .collect(Collectors.toList());

        PageResponse<SessionEventResponse> result = PageResponse.of(
                events, events.size(), page, size);

        log.info("API /api/sessions/{}/events success, items={}", sessionId, events.size());
        return result;
    }

    // ==================== Traces ====================

    @GetMapping("/traces")
    public PageResponse<TraceResponse> getTraces(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @RequestParam(name = "statusCode", required = false) String statusCode,
            @RequestParam(name = "search", required = false) String search) {
        log.info("API Call: /api/traces, page={}, size={}", page, size);

        Pageable pageable = PageableHelper.createDescByCreatedAt(page, size);
        Page<OtelTraceDocument> tracePage = otelTraceEsRepository.findAll(pageable);
        List<TraceResponse> traceList = PageableHelper.convertPageToList(tracePage, TraceResponse::from);

        PageResponse<TraceResponse> result = PageResponse.of(
                traceList, tracePage.getTotalElements(), page, size);

        log.info("API /api/traces success, items={}, total={}", traceList.size(), tracePage.getTotalElements());
        return result;
    }

    @GetMapping("/traces/{id}")
    public ResponseEntity<TraceResponse> getTrace(@PathVariable String id) {
        log.info("API Call: /api/traces/{}", id);
        Optional<OtelTraceDocument> trace = otelTraceEsRepository.findById(id);
        if (trace.isPresent()) {
            return ResponseEntity.ok(TraceResponse.from(trace.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== Metrics ====================

    @GetMapping("/metrics")
    public MetricsResponse getMetrics(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "metricName", required = false) String metricName,
            @RequestParam(name = "serviceName", required = false) String serviceName) {
        log.info("API Call: /api/metrics, page={}, size={}", page, size);

        Pageable pageable = PageableHelper.createDescByCreatedAt(page, size);
        Page<OtelMetricDocument> metricPage = otelMetricEsRepository.findAll(pageable);
        List<MetricResponse> metricList = PageableHelper.convertPageToList(metricPage, MetricResponse::from);

        List<String> metricNames = otelMetricEsRepository.findDistinctMetricNames();

        int totalPages = size > 0 ? (int) Math.ceil((double) metricPage.getTotalElements() / size) : 0;
        MetricsResponse result = MetricsResponse.builder()
                .items(metricList)
                .total(metricPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .metricNames(metricNames)
                .build();

        log.info("API /api/metrics success, items={}, total={}", metricList.size(), metricPage.getTotalElements());
        return result;
    }

    @GetMapping("/metrics/chart")
    public MetricChartResponse getMetricChartData(
            @RequestParam(name = "metricName") String metricName,
            @RequestParam(name = "timeRange", defaultValue = "1h") String timeRange) {
        log.info("API Call: /api/metrics/chart, metricName={}, timeRange={}", metricName, timeRange);

        List<String> times = new ArrayList<>();
        List<Double> values = new ArrayList<>();

        MetricChartResponse result = MetricChartResponse.builder()
                .times(times)
                .values(values)
                .metricName(metricName)
                .build();

        log.info("API /api/metrics/chart success, data points={}", times.size());
        return result;
    }

    @GetMapping("/metrics/names")
    public List<String> getMetricNames() {
        log.info("API Call: /api/metrics/names");
        List<String> names = otelMetricEsRepository.findDistinctMetricNames();
        names.sort(String::compareTo);
        log.info("API /api/metrics/names success, names={}", names);
        return names;
    }

    // ==================== Logs ====================

    @GetMapping("/logs")
    public PageResponse<LogResponse> getLogs(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "logLevel", required = false) String logLevel,
            @RequestParam(name = "serviceName", required = false) String serviceName,
            @RequestParam(name = "search", required = false) String search) {
        log.info("API Call: /api/logs, page={}, size={}", page, size);

        Pageable pageable = PageableHelper.createDescByCreatedAt(page, size);
        Page<OtelLogDocument> logPage = otelLogEsRepository.findAll(pageable);
        List<LogResponse> logList = PageableHelper.convertPageToList(logPage, LogResponse::from);

        PageResponse<LogResponse> result = PageResponse.of(
                logList, logPage.getTotalElements(), page, size);

        log.info("API /api/logs success, items={}, total={}", logList.size(), logPage.getTotalElements());
        return result;
    }

    @GetMapping("/logs/{id}")
    public ResponseEntity<LogResponse> getLog(@PathVariable String id) {
        log.info("API Call: /api/logs/{}", id);
        Optional<OtelLogDocument> logDoc = otelLogEsRepository.findById(id);
        if (logDoc.isPresent()) {
            return ResponseEntity.ok(LogResponse.from(logDoc.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
