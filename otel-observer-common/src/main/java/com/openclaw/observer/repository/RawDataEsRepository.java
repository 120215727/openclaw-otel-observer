package com.openclaw.observer.repository;

import com.openclaw.observer.common.enums.ProcessingStatus;
import com.openclaw.observer.common.enums.RawType;
import com.openclaw.observer.document.RawDataDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RawDataEsRepository extends ElasticsearchRepository<RawDataDocument, String> {

    /**
     * 根据 raw_type 查找
     */
    List<RawDataDocument> findByRawType(RawType rawType);

    /**
     * 根据 processing_status 查找
     */
    List<RawDataDocument> findByProcessingStatus(ProcessingStatus processingStatus);

    /**
     * 根据 session_id 查找
     */
    List<RawDataDocument> findBySessionId(String sessionId);

    /**
     * 根据 agent_id 查找
     */
    List<RawDataDocument> findByAgentId(String agentId);

    /**
     * 查找处理失败的
     */
    List<RawDataDocument> findByProcessingStatusOrderByCreatedAtDesc(ProcessingStatus processingStatus);

    /**
     * 查找待处理的
     */
    List<RawDataDocument> findByProcessingStatusOrderByCreatedAtAsc(ProcessingStatus processingStatus);
}
