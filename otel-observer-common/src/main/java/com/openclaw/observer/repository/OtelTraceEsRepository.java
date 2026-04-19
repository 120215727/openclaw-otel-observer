package com.openclaw.observer.repository;

import com.openclaw.observer.document.OtelTraceDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtelTraceEsRepository extends ElasticsearchRepository<OtelTraceDocument, String>, CustomOtelTraceEsRepository {

    List<OtelTraceDocument> findByTraceId(String traceId);

    List<OtelTraceDocument> findByServiceName(String serviceName);
}
