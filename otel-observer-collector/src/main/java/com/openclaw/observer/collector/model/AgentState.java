package com.openclaw.observer.collector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 同步状态
 * <p>
 * 记录单个 Agent 的会话同步状态，包括 sessions.json 和各个会话 jsonl 文件的处理进度。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentState {

    /**
     * Agent 标识
     */
    private String agentId;

    /**
     * Session 记录更新时间映射
     * <p>
     * Key: sessionId, Value: updatedAt（从 sessions.json 中读取）
     * <p>
     * 用于检测 sessions.json 中的会话元数据是否发生变化。每次成功上传会话元数据后更新此映射，
     * 下次扫描时若发现 updatedAt 未变化则跳过该会话的元数据同步。
     */
    private Map<String, Long> sessionIdRecordUpdatedAt = new HashMap<>();

    /**
     * JSONL 会话文件状态映射
     * <p>
     * Key: 文件名（如 {session_id}.jsonl）, Value: 文件处理状态
     * <p>
     * 处理逻辑：
     * <ol>
     *   <li>验证完整性：计算文件第 1 行到 lastReadLine 的 SHA-256 签名，与 processedLinesSha 比对。
     *       若不匹配，说明已处理内容被修改，需重置状态并全量重新上传。</li>
     *   <li>增量读取：若签名匹配，从 lastReadLine 之后读取新行。</li>
     *   <li>上传新记录：对新行进行 JSON 有效性校验，有效则上传并递增 lastReadLine，
     *       同时重新计算第 1 行到新 lastReadLine 的 SHA 签名。</li>
     *   <li>处理不完整记录：若遇到无效 JSON（可能是写入中的记录），记录 warn 日志并停止处理，
     *       等待下次扫描。</li>
     * </ol>
     */
    private Map<String, SessionFileState> jsonlSessionFilesState = new HashMap<>();

    /**
     * 创建指定 Agent 的初始状态
     *
     * @param agentId Agent 标识
     * @return 初始化的 AgentState 实例
     */
    public static AgentState from(String agentId) {
        AgentState agentState = new AgentState();
        agentState.setAgentId(agentId);
        return agentState;
    }
}
