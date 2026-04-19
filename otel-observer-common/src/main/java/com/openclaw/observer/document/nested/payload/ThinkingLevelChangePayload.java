package com.openclaw.observer.document.nested.payload;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Thinking Level Change 事件详情
 */
@Data
public class ThinkingLevelChangePayload {
    /**
     * 思考级别
     */
    @Field(type = FieldType.Keyword, name = "thinking_level")
    private String thinkingLevel;

    /**
     * 列表显示简介
     */
    @Field(type = FieldType.Text, name = "text")
    private String text;
}
