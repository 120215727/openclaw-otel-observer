package com.openclaw.observer.collector.service.processor;

import com.openclaw.observer.collector.model.AgentState;
import com.openclaw.observer.collector.model.SessionFileState;
import com.openclaw.observer.collector.service.SessionUploadService;
import com.openclaw.observer.collector.util.FileTypeDetector;
import com.openclaw.observer.collector.util.JsonlFileUtil;
import com.openclaw.observer.common.enums.SessionFileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionJsonlProcessor {

    private final JsonlFileUtil jsonlFileUtil;
    private final SessionUploadService uploadService;

    public void processSessionFile(AgentState agentState, File sessionFile) {
        String fileName = sessionFile.getName();
        String sessionId = FileTypeDetector.extractSessionId(fileName);
        SessionFileType fileType = FileTypeDetector.detect(sessionFile);

        log.debug("检查文件: {} (sessionId: {}, type: {})", fileName, sessionId, fileType.getValue());

        // 获取或创建文件状态
        SessionFileState fileState = agentState.getJsonlSessionFilesState().computeIfAbsent(fileName, k -> SessionFileState.builder().lastReadLine(0L).processedLinesSha(null).build());

        // 获取文件总行数
        long totalLines = jsonlFileUtil.getTotalLineCount(sessionFile);

        // 检查已处理的内容是否被修改（SHA 验证）
        boolean needsFullUpload = false;
        if (fileState.getLastReadLine() > 0 && fileState.getProcessedLinesSha() != null) {
            String currentSha = jsonlFileUtil.calculateSha256ForLines(sessionFile, fileState.getLastReadLine());
            if (currentSha == null || !currentSha.equals(fileState.getProcessedLinesSha())) {
                log.warn("文件 {} 的已处理内容被修改 (lastReadLine: {})，需要全量重新上传", fileName, fileState.getLastReadLine());
                needsFullUpload = true;
                // 重置状态
                fileState.setLastReadLine(0L);
                fileState.setProcessedLinesSha(null);
            }
        }

        // 确定起始行
        long startLine = needsFullUpload ? 0L : fileState.getLastReadLine();

        // 如果没有新内容，跳过
        if (startLine >= totalLines) {
            log.debug("文件 {} 无新内容，跳过", fileName);
            return;
        }

        // 读取新行
        List<String> newLines = jsonlFileUtil.readLinesFrom(sessionFile, startLine);
        if (newLines.isEmpty()) {
            log.debug("文件 {} 没有可读取的新行", fileName);
            return;
        }

        log.info("处理文件 {}: 从第 {} 行开始，共 {} 条新记录", fileName, startLine, newLines.size());

        // 处理并上传新行
        long validLineCount = 0;
        int uploadedCount = 0;

        for (String line : newLines) {
            if (line.trim().isEmpty()) {
                validLineCount++;
                continue;
            }

            // 验证 JSON 有效性
            if (!jsonlFileUtil.isValidJson(line)) {
                log.warn("文件 {} 的第 {} 行不是有效的 JSON，可能是写入中的记录，忽略: {}", fileName, startLine + validLineCount + 1, line.substring(0, Math.min(100, line.length())));
                // 遇到无效 JSON 停止处理，等待下次扫描
                break;
            }

            long lineNo = startLine + validLineCount;

            // 上传
            boolean success = uploadService.uploadSessionEvent(agentState.getAgentId(), sessionId, fileType, fileName, lineNo, line);
            if (success) {
                uploadedCount++;
            }

            validLineCount++;
        }

        // 更新状态
        if (validLineCount > 0) {
            long newLastReadLine = startLine + validLineCount;
            String newSha = jsonlFileUtil.calculateSha256ForLines(sessionFile, newLastReadLine);

            fileState.setLastReadLine(newLastReadLine);
            fileState.setProcessedLinesSha(newSha);

            log.info("✅ 文件 {} 处理完成: 上传 {} 条新记录，lastReadLine 更新为 {}", fileName, uploadedCount, newLastReadLine);
        }
    }
}
