package com.openclaw.observer.repository;

import com.openclaw.observer.document.SessionEventDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionEventEsRepository extends ElasticsearchRepository<SessionEventDocument, String> {

    List<SessionEventDocument> findBySessionId(String sessionId);

    List<SessionEventDocument> findBySessionIdOrderByEventTimestampAsc(String sessionId);

    List<SessionEventDocument> findBySessionIdAndEventType(String sessionId, String eventType);

    List<SessionEventDocument> findByAgentId(String agentId);

    List<SessionEventDocument> findByAgentIdAndEventType(String agentId, String eventType);

    List<SessionEventDocument> findBySessionIdAndMessageRole(String sessionId, String messageRole);

    boolean existsByEventId(String eventId);
}
