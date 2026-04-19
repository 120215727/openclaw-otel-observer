package com.openclaw.observer.common;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理结果类 - 通用
 *
 * 用于记录处理过程中解析和存储的数据统计
 */
@Data
public class ProcessResult {

    // 解析数量
    public long parsedSessionCount = 0;
    public long parsedMessageCount = 0;
    public long parsedEventCount = 0;
    public long parsedLogCount = 0;
    public long parsedTraceCount = 0;
    public long parsedMetricCount = 0;

    // 实际存储数量
    public long storedSessionCount = 0;
    public long storedMessageCount = 0;
    public long storedEventCount = 0;
    public long storedLogCount = 0;
    public long storedTraceCount = 0;
    public long storedMetricCount = 0;

    // 生成的文档 ID 列表
    public List<String> sessionIds = new ArrayList<>();
    public List<String> messageIds = new ArrayList<>();
    public List<String> eventIds = new ArrayList<>();
    public List<String> logIds = new ArrayList<>();
    public List<String> traceIds = new ArrayList<>();
    public List<String> metricIds = new ArrayList<>();

    /**
     * 增加 session 计数
     */
    public void addSession(String id) {
        sessionIds.add(id);
        parsedSessionCount++;
        storedSessionCount++;
    }

    /**
     * 增加 message 计数
     */
    public void addMessage(String id) {
        messageIds.add(id);
        parsedMessageCount++;
        storedMessageCount++;
    }

    /**
     * 增加 event 计数
     */
    public void addEvent(String id) {
        eventIds.add(id);
        parsedEventCount++;
        storedEventCount++;
    }

    /**
     * 增加 log 计数
     */
    public void addLog(String id) {
        logIds.add(id);
        parsedLogCount++;
        storedLogCount++;
    }

    /**
     * 增加 trace 计数
     */
    public void addTrace(String id) {
        traceIds.add(id);
        parsedTraceCount++;
        storedTraceCount++;
    }

    /**
     * 增加 metric 计数
     */
    public void addMetric(String id) {
        metricIds.add(id);
        parsedMetricCount++;
        storedMetricCount++;
    }
}
