package com.openclaw.observer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ElasticsearchConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 强制日期序列化为完整格式，包括时间
        mapper.setDateFormat(new com.fasterxml.jackson.databind.util.StdDateFormat()
                .withColonInTimeZone(true)
                .withTimeZone(java.util.TimeZone.getTimeZone("Asia/Shanghai")));
        return mapper;
    }
}
