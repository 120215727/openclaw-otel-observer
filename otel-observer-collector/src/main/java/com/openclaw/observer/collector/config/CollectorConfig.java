package com.openclaw.observer.collector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.observer.collector.util.JsonlFileUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CollectorConfig {

    @Bean
    public JsonlFileUtil jsonlFileUtil(ObjectMapper objectMapper) {
        return new JsonlFileUtil(objectMapper);
    }

    @Bean
    public WebClient receiverWebClient(CollectorProperties properties) {
        return WebClient.builder().baseUrl(properties.getReceiverUrl()).build();
    }
}
