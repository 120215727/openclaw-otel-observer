package com.openclaw.observer.document.nested.payload;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Session 事件详情
 */
@Data
public class SessionPayload {
    /**
     * JSONL 版本号
     */
    @Field(type = FieldType.Integer, name = "version")
    private Integer version;

    /**
     * 工作目录
     */
    @Field(type = FieldType.Keyword, name = "cwd")
    private String cwd;

    /**
     * 列表显示简介
     */
    @Field(type = FieldType.Text, name = "text")
    private String text;
}
