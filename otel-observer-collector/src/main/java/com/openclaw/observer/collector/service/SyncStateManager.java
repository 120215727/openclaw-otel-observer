package com.openclaw.observer.collector.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.collector.config.CollectorProperties;
import com.openclaw.observer.collector.model.AgentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncStateManager {

    private final CollectorProperties properties;
    private final ObjectMapper objectMapper;

    private Map<String, AgentState> syncState;

    public void loadState() {
        try {
            Path statePath = Paths.get(properties.getStatePath());
            if (Files.exists(statePath)) {
                String json = Files.readString(statePath);
                syncState = objectMapper.readValue(json, new TypeReference<Map<String, AgentState>>() {});
                log.info("已加载同步状态，包含 {} 个agent", syncState.size());
            } else {
                syncState = new HashMap<>();
                log.info("创建新的同步状态");
            }
        } catch (Exception e) {
            log.warn("加载同步状态失败，创建新状态: {}", e.getMessage());
            syncState = new HashMap<>();
        }
    }

    public void saveState() {
        try {
            Path statePath = Paths.get(properties.getStatePath());
            Path parentDir = statePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(syncState);
            Files.writeString(statePath, json);
            log.debug("同步状态已保存");
        } catch (Exception e) {
            log.warn("保存同步状态失败: {}", e.getMessage());
        }
    }

    public Map<String, AgentState> getSyncState() {
        if (syncState == null) {
            loadState();
        }
        return syncState;
    }

}
