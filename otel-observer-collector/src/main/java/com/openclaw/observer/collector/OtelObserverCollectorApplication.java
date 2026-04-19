package com.openclaw.observer.collector;

import com.openclaw.observer.collector.service.SessionScannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@SpringBootApplication(scanBasePackages = "com.openclaw.observer.collector")
@EnableScheduling
public class OtelObserverCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtelObserverCollectorApplication.class, args);
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
