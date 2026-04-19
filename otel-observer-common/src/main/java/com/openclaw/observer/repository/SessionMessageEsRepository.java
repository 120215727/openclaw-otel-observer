package com.openclaw.observer.repository;

import com.openclaw.observer.document.SessionMessageDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionMessageEsRepository extends ElasticsearchRepository<SessionMessageDocument, String> {

    /**
     * 根据 sessionId 查找所有消息
     */
    List<SessionMessageDocument> findBySessionIdOrderByEventTimestampAsc(String sessionId);

    /**
     * 根据 eventId 查找
     */
    Optional<SessionMessageDocument> findByEventId(String eventId);

    /**
     * 根据 sessionId + eventType 查找
     */
    List<SessionMessageDocument> findBySessionIdAndEventType(String sessionId, String eventType);

    /**
     * 统计某个 session 的消息数
     */
    long countBySessionId(String sessionId);

    /**
     * 删除某个 session 的所有消息
     */
    void deleteBySessionId(String sessionId);
}
