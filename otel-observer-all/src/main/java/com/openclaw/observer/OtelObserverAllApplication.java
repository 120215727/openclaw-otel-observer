package com.openclaw.observer;

import com.openclaw.observer.collector.service.SessionScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * Otel Observer All-in-One Application
 * <p>
 * 合并 Collector 和 Receiver 的二合一服务
 * - Receiver: 接收 OTLP 和 Session 数据，存储到 ES
 * - Collector: 扫描本地 session 文件，发送给 Receiver
 * <p>
 * 包扫描包含：
 * - com.openclaw.observer (Receiver 相关)
 * - com.openclaw.observer.collector (Collector 相关)
 */
@SpringBootApplication(scanBasePackages = {
    "com.openclaw.observer",
    "com.openclaw.observer.collector"
})
@EnableElasticsearchRepositories(basePackages = "com.openclaw.observer.repository")
@EnableScheduling
public class OtelObserverAllApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtelObserverAllApplication.class, args);
    }

    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class ApplicationStartup {

        private final SessionScannerService sessionScannerService;

        @EventListener(ApplicationReadyEvent.class)
        public void onApplicationReady() {
            log.info("应用启动完成，执行初始扫描...");
            sessionScannerService.scanAndSync();
        }
    }
}
