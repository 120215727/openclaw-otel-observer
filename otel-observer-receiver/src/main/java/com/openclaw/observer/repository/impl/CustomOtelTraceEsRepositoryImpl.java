package com.openclaw.observer.repository.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.openclaw.observer.document.OtelTraceDocument;
import com.openclaw.observer.repository.CustomOtelTraceEsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CustomOtelTraceEsRepositoryImpl implements CustomOtelTraceEsRepository {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public List<String> findDistinctServiceNames() {
        List<String> result = new ArrayList<>();
        try {
            SearchResponse<OtelTraceDocument> response = elasticsearchClient.search(s -> s
                    .index("otel-traces")
                    .size(0)
                    .aggregations("service_names", a -> a
                            .terms(t -> t
                                    .field("serviceName")
                                    .size(100)
                            )
                    ),
                    OtelTraceDocument.class
            );

            var agg = response.aggregations().get("service_names");
            if (agg != null && agg.isSterms()) {
                for (StringTermsBucket bucket : agg.sterms().buckets().array()) {
                    result.add(bucket.key().stringValue());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get distinct service names", e);
        }
        return result;
    }
}
