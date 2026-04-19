package com.openclaw.observer.repository;

import com.openclaw.observer.document.OtelMetricDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtelMetricEsRepository extends ElasticsearchRepository<OtelMetricDocument, String>, CustomOtelMetricEsRepository {

    List<OtelMetricDocument> findByMetricName(String metricName);

    List<OtelMetricDocument> findByServiceName(String serviceName);
}
