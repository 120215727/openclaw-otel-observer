package com.openclaw.observer.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "collector")
public class CollectorProperties {

    /**
     * OpenClaw agents根目录
     */
    private String agentsPath = System.getProperty("user.home") + "/.openclaw/agents";

    /**
     * Receiver服务地址
     */
    private String receiverUrl = "http://localhost:10333";

    /**
     * 扫描间隔（毫秒）
     */
    private long scanIntervalMs = 30000;

    /**
     * 状态文件存储路径
     */
    private String statePath = "data/collector-state.json";

    /**
     * 是否启用自动扫描
     */
    private boolean autoScanEnabled = true;
}
