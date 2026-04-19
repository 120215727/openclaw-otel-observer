package com.openclaw.observer.service.processor;

import com.openclaw.observer.document.RawDataDocument;

/**
 * RawData 处理器接口
 * 用于处理不同类型的 RawDataDocument
 */
public interface RawDataProcessor {

    /**
     * 处理 RawDataDocument
     *
     * @param rawDoc 原始数据文档
     */
    void process(RawDataDocument rawDoc);
}
