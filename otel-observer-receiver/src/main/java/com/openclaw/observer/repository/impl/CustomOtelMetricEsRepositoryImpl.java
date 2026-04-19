package com.openclaw.observer.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.openclaw.observer.document.OtelMetricDocument;
import com.openclaw.observer.repository.CustomOtelMetricEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomOtelMetricEsRepositoryImpl implements CustomOtelMetricEsRepository {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public List<String> findDistinctMetricNames() {
        List<String> result = new ArrayList<>();
        try {
            SearchResponse<OtelMetricDocument> response = elasticsearchClient.search(s -> s
                            .index("otel-metrics")
                            .size(0)
                            .aggregations("metric_names", a -> a
                                    .terms(t -> t
                                            .field("metricName")
                                            .size(100)
                                    )
                            ),
                    OtelMetricDocument.class
            );

            var agg = response.aggregations().get("metric_names");
            if (agg != null && agg.isSterms()) {
                for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
                    result.add(bucket.key().stringValue());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get distinct metric names", e);
        }
        return result;
    }

    @Override
    public Map<String, Long> getTopMetricCounts(int limit) {
        Map<String, Long> result = new HashMap<>();
        try {
            SearchResponse<OtelMetricDocument> response = elasticsearchClient.search(s -> s
                            .index("otel-metrics")
                            .size(0)
                            .aggregations("metric_counts", a -> a
                                    .terms(t -> t
                                            .field("metricName")
                                            .size(limit)
                                    )
                            ),
                    OtelMetricDocument.class
            );

            var agg = response.aggregations().get("metric_counts");
            if (agg != null && agg.isSterms()) {
                for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
                    result.put(bucket.key().stringValue(), bucket.docCount());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get top metric counts", e);
        }
        return result;
    }
}
