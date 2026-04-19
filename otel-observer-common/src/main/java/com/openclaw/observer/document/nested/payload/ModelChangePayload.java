package com.openclaw.observer.document.nested.payload;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Model Change 事件详情
 */
@Data
public class ModelChangePayload {
    /**
     * 模型提供商
     */
    @Field(type = FieldType.Keyword, name = "provider")
    private String provider;

    /**
     * 模型 ID
     */
    @Field(type = FieldType.Keyword, name = "model_id")
    private String modelId;

    /**
     * 列表显示简介
     */
    @Field(type = FieldType.Text, name = "text")
    private String text;
}
