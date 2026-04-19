package com.openclaw.observer.repository;

import com.openclaw.observer.document.SessionDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionEsRepository extends ElasticsearchRepository<SessionDocument, String> {

    Optional<SessionDocument> findBySessionId(String sessionId);

    Optional<SessionDocument> findByAgentIdAndSessionId(String agentId, String sessionId);

    List<SessionDocument> findByAgentId(String agentId);

    List<SessionDocument> findByAgentIdOrderBySessionTimestampDesc(String agentId);

    boolean existsBySessionId(String sessionId);
}
