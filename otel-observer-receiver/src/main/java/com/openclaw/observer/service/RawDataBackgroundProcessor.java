package com.openclaw.observer.service;

import com.openclaw.observer.common.enums.ProcessingStatus;
import com.openclaw.observer.common.enums.RawType;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.repository.RawDataEsRepository;
import com.openclaw.observer.service.processor.RawDataProcessor;
import com.openclaw.observer.service.processor.collector.SessionMessageProcessor;
import com.openclaw.observer.service.processor.collector.SessionMetadataProcessor;
import com.openclaw.observer.service.processor.otel.OtlpLogsProcessor;
import com.openclaw.observer.service.processor.otel.OtlpMetricsProcessor;
import com.openclaw.observer.service.processor.otel.OtlpTracesProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * RawData 后台处理器 - 异步处理 pending 的 RawData
 * <p>
 * 设计思路：
 * 1. 请求入口只快速创建 RawDataDocument (status = pending)，立即返回
 * 2. 后台线程定时扫描 pending 的记录并处理
 * 3. failed 的记录不再自动重试（通过手动接口重试）
 * 4. 可以持续增加支持的类型
 */
@Slf4j
@Service
public class RawDataBackgroundProcessor {

    private final RawDataEsRepository rawDataRepository;
    private final RawDataProcessorService rawDataProcessorService;

    // Processor 映射
    private final Map<RawType, RawDataProcessor> processors;

    public RawDataBackgroundProcessor(
            RawDataEsRepository rawDataRepository,
            RawDataProcessorService rawDataProcessorService,
            OtlpLogsProcessor otlpLogsProcessor,
            OtlpMetricsProcessor otlpMetricsProcessor,
            OtlpTracesProcessor otlpTracesProcessor,
            SessionMetadataProcessor sessionMetadataProcessor,
            SessionMessageProcessor sessionMessageProcessor) {
        this.rawDataRepository = rawDataRepository;
        this.rawDataProcessorService = rawDataProcessorService;
        this.processors = Map.of(
//                RawType.OTLP_LOGS, otlpLogsProcessor
//                RawType.OTLP_METRICS, otlpMetricsProcessor
                RawType.OTLP_TRACES, otlpTracesProcessor
//                RawType.SESSION_METADATA, sessionMetadataProcessor
//                RawType.SESSION_EVENT, sessionMessageProcessor
        );
    }

    /**
     * 定时扫描并处理 pending 的 RawData
     * 每 5 秒执行一次
     */
    @Scheduled(fixedDelayString = "${rawdata.processor.interval-ms:5000}")
    public void processPendingRawData() {
        try {
            // 查找 pending 的记录，按接收时间升序（先到先处理）
            List<RawDataDocument> pendingList = rawDataRepository
                    .findByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus.PENDING);

            if (pendingList.isEmpty()) {
                log.info("没有待处理的 RawData");
                return;
            }

            log.info("发现 {} 条待处理的 RawData，开始处理...", pendingList.size());

            int successCount = 0;
            int failCount = 0;

            for (RawDataDocument rawDoc : pendingList) {
                try {
                    processSingleRawData(rawDoc);
                    successCount++;
                } catch (Exception e) {
                    log.error("处理 RawData 失败 (id: {}): {}", rawDoc.getId(), e.getMessage(), e);
                    markAsFailed(rawDoc, e);
                    failCount++;
                }
            }

            log.info("RawData 处理完成：成功 {} 条，失败 {} 条", successCount, failCount);

        } catch (Exception e) {
            log.error("扫描 pending RawData 异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理单条 RawData
     */
    @Transactional
    public void processSingleRawData(RawDataDocument rawDoc) {
        log.info("开始处理 RawData (id: {}, type: {})", rawDoc.getId(), rawDoc.getRawType());

        // 先标记为 processing（防止重复处理）
        markAsProcessing(rawDoc);

        // 根据类型获取 processor
        RawDataProcessor processor = processors.get(rawDoc.getRawType());
        if (processor == null) {
            throw new UnsupportedOperationException("未知的 raw_type: " + rawDoc.getRawType());
        }

        // 处理
        processor.process(rawDoc);

        // 更新为 success
        updateToSuccess(rawDoc);
        log.info("✅ RawData 处理成功 (id: {})", rawDoc.getId());
    }

    // ==================== 状态更新方法 ====================

    private void markAsProcessing(RawDataDocument rawDoc) {
        rawDoc.setProcessingStatus(ProcessingStatus.PROCESSING);
        rawDoc.setProcessingStartedAt(LocalDateTime.now(com.openclaw.observer.common.ObserverConstants.ZONE_SHANGHAI)
                .format(com.openclaw.observer.common.ObserverConstants.ES_DATE_FORMAT));
        rawDataRepository.save(rawDoc);
    }

    private void updateToSuccess(RawDataDocument rawDoc) {
        rawDoc.setProcessingStatus(ProcessingStatus.SUCCESS);
        rawDoc.setProcessingCompletedAt(LocalDateTime.now(com.openclaw.observer.common.ObserverConstants.ZONE_SHANGHAI)
                .format(com.openclaw.observer.common.ObserverConstants.ES_DATE_FORMAT));
        rawDoc.setUpdatedAt(LocalDateTime.now(com.openclaw.observer.common.ObserverConstants.ZONE_SHANGHAI)
                .format(com.openclaw.observer.common.ObserverConstants.ES_DATE_FORMAT));
        rawDataRepository.save(rawDoc);
    }

    private void markAsFailed(RawDataDocument rawDoc, Exception e) {
        rawDoc.setProcessingStatus(ProcessingStatus.FAILED);
        rawDoc.setProcessingCompletedAt(LocalDateTime.now(com.openclaw.observer.common.ObserverConstants.ZONE_SHANGHAI)
                .format(com.openclaw.observer.common.ObserverConstants.ES_DATE_FORMAT));
        rawDoc.setErrorMessage(e.getMessage());

        StringWriter sw = new StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        rawDoc.setErrorStackTrace(sw.toString());

        rawDoc.setUpdatedAt(LocalDateTime.now(com.openclaw.observer.common.ObserverConstants.ZONE_SHANGHAI)
                .format(com.openclaw.observer.common.ObserverConstants.ES_DATE_FORMAT));

        rawDataRepository.save(rawDoc);
        log.warn("RawData 处理失败 (id: {}): {}", rawDoc.getId(), e.getMessage());
    }
}
