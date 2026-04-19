package com.openclaw.observer.repository;

import com.openclaw.observer.document.OtelLogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtelLogEsRepository extends ElasticsearchRepository<OtelLogDocument, String>, CustomOtelLogEsRepository {

    List<OtelLogDocument> findByTraceId(String traceId);

    List<OtelLogDocument> findByServiceName(String serviceName);

    List<OtelLogDocument> findByLogLevel(String logLevel);
}
