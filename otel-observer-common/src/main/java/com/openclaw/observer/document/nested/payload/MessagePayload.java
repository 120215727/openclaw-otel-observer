package com.openclaw.observer.document.nested.payload;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Message 事件详情
 */
@Data
public class MessagePayload {
    /**
     * 消息角色
     * user/assistant/toolCall/toolResult
     */
    @Field(type = FieldType.Keyword, name = "role")
    private String role;

    /**
     * 消息时间戳（message内部的timestamp）
     */
    @Field(type = FieldType.Long, name = "timestamp")
    private Long timestamp;

    /**
     * 消息完整 content 数组 JSON
     */
    @Field(type = FieldType.Text, name = "content_json")
    private String rawContentJsonArray;

    /**
     * 消息纯文本,尽量提取（用于列表显示简介）
     */
    @Field(type = FieldType.Text, name = "text")
    private String text;

    /**
     * 消息列表数组（text/toolCall/toolResult/image等）
     */
    @Field(type = FieldType.Keyword, name = "content_type")
    private String contentType;

}
