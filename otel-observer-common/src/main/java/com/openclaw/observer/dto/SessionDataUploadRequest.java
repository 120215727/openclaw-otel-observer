package com.openclaw.observer.dto;

import com.openclaw.observer.common.enums.SessionFileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 会话数据上传请求（统一用于元数据和事件）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDataUploadRequest {

    /**
     * Agent 标识
     */
    private String agentId;

    /**
     * Session 标识
     */
    private String sessionId;

    /**
     * 文件类型
     */
    private SessionFileType fileType;

    /**
     * 文件名（可选）
     */
    private String fileName;

    /**
     * 行号（仅会话事件时有值，从 0 开始）
     */
    private Long lineNo;

    /**
     * 数据内容（JSON 格式）
     * <p>
     * - 元数据上传：sessions.json 中的单个 session 记录
     * - 事件上传：jsonl 文件中的单行事件
     */
    private String dataJson;

    /**
     * 创建元数据上传请求
     */
    public static SessionDataUploadRequest forMetadata(String agentId, String sessionId,
                                                       String fileName, String dataJson) {
        return SessionDataUploadRequest.builder()
                .agentId(agentId)
                .sessionId(sessionId)
                .fileType(SessionFileType.SESSIONS_JSON)
                .fileName(fileName)
                .dataJson(dataJson)
                .build();
    }

    /**
     * 创建事件上传请求
     */
    public static SessionDataUploadRequest forEvent(String agentId, String sessionId,
                                                    SessionFileType fileType, String fileName,
                                                    Long lineNo, String dataJson) {
        return SessionDataUploadRequest.builder()
                .agentId(agentId)
                .sessionId(sessionId)
                .fileType(fileType)
                .fileName(fileName)
                .lineNo(lineNo)
                .dataJson(dataJson)
                .build();
    }
}
