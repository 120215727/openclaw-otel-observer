package com.openclaw.observer.collector.service;

import com.openclaw.observer.collector.config.CollectorProperties;
import com.openclaw.observer.collector.model.AgentState;
import com.openclaw.observer.collector.service.processor.SessionJsonlProcessor;
import com.openclaw.observer.collector.service.processor.SessionMetadataProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionScannerService {

    private final CollectorProperties properties;
    private final SyncStateManager syncStateManager;
    private final SessionMetadataProcessor sessionMetadataProcessor;
    private final SessionJsonlProcessor sessionJsonlProcessor;

    @Scheduled(fixedDelayString = "${collector.scan-interval-ms:30000}")
    public synchronized void scanAndSync() {
        if (!properties.isAutoScanEnabled()) {
            return;
        }

        log.info("\n" + "═══════════════════════════════════════════════════════════════\n" + "🔍 开始扫描 Session 文件...\n" + "═══════════════════════════════════════════════════════════════");

        try {
            syncStateManager.getSyncState();

            File agentsDir = new File(properties.getAgentsPath());
            if (!agentsDir.exists() || !agentsDir.isDirectory()) {
                log.warn("Agents目录不存在: {}", properties.getAgentsPath());
                return;
            }

            File[] agentDirs = agentsDir.listFiles(File::isDirectory);
            if (agentDirs == null) {
                log.info("没有找到agent目录");
                return;
            }

            for (File agentDir : agentDirs) {
                scanAgent(agentDir);
            }

            syncStateManager.saveState();

            log.info("\n" + "═══════════════════════════════════════════════════════════════\n" + "✅ 扫描完成\n" + "═══════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("❌ 扫描失败: {}", e.getMessage(), e);
        }
    }

    private void scanAgent(File agentDir) {
        String agentId = agentDir.getName();
        log.info("扫描Agent: {}", agentId);

        AgentState agentState = syncStateManager.getSyncState().computeIfAbsent(agentId, AgentState::from);
        File sessionsDir = new File(agentDir, "sessions");

        sessionMetadataProcessor.processSessionsJson(sessionsDir, agentState);

        if (!sessionsDir.exists() || !sessionsDir.isDirectory()) {
            log.debug("Agent {} 没有sessions目录", agentId);
            return;
        }

        File[] sessionFiles = sessionsDir.listFiles((dir, name) -> name.endsWith(".jsonl"));
        if (sessionFiles == null) {
            return;
        }

        for (File sessionFile : sessionFiles) {
            sessionJsonlProcessor.processSessionFile(agentState, sessionFile);
        }
    }
}
