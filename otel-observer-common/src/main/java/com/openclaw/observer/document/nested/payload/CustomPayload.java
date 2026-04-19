package com.openclaw.observer.document.nested.payload;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Custom 事件详情
 */
@Data
public class CustomPayload {
    /**
     * 自定义事件类型
     */
    @Field(type = FieldType.Keyword, name = "custom_type")
    private String customType;

    /**
     * 自定义事件数据 JSON
     */
    @Field(type = FieldType.Text, name = "data_json")
    private String dataJson;

    /**
     * 列表显示简介
     */
    @Field(type = FieldType.Text, name = "text")
    private String text;
}
