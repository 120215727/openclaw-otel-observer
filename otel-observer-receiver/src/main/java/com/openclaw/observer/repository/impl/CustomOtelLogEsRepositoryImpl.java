package com.openclaw.observer.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.openclaw.observer.document.OtelLogDocument;
import com.openclaw.observer.repository.CustomOtelLogEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomOtelLogEsRepositoryImpl implements CustomOtelLogEsRepository {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public Map<String, Long> getLogLevelCounts() {
        Map<String, Long> result = new HashMap<>();
        result.put("INFO", 0L);
        result.put("WARN", 0L);
        result.put("ERROR", 0L);
        result.put("DEBUG", 0L);

        try {
            SearchResponse<OtelLogDocument> response = elasticsearchClient.search(s -> s
                            .index("otel-logs")
                            .size(0)
                            .aggregations("log_levels", a -> a
                                    .terms(t -> t
                                            .field("logLevel")
                                            .size(10)
                                    )
                            ),
                    OtelLogDocument.class
            );

            var agg = response.aggregations().get("log_levels");
            if (agg != null && agg.isSterms()) {
                for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
                    String level = bucket.key().stringValue().toUpperCase();
                    result.put(level, bucket.docCount());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get log level counts", e);
        }
        return result;
    }
}
