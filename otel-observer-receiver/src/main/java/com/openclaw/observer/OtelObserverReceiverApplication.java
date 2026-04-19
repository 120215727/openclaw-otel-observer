package com.openclaw.observer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.openclaw.observer")
@EnableElasticsearchRepositories(basePackages = "com.openclaw.observer.repository")
@EnableScheduling
public class OtelObserverReceiverApplication {

    public static void main(String[] args) {
        SpringApplication.run(OtelObserverReceiverApplication.class, args);
    }
}
