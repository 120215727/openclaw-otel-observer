package com.openclaw.observer.collector.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JSONL 会话文件处理状态
 * <p>
 * 记录单个会话文件的读取进度和内容完整性校验信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFileState {

    /**
     * 已成功处理的最后一行行号（从 0 开始）
     * <p>
     * 表示文件中 [0, lastReadLine) 范围的行已完成处理和上传。
     * 下次扫描将从 lastReadLine 位置开始读取新内容。
     */
    private long lastReadLine;

    /**
     * 已处理内容的 SHA-256 签名
     * <p>
     * 对文件第 1 行到 lastReadLine 的内容计算 SHA-256 签名，
     * 用于验证已处理内容是否被篡改。若签名不匹配，需全量重新上传。
     */
    private String processedLinesSha;
}
