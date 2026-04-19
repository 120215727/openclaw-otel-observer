package com.openclaw.observer.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 处理状态枚举
 *
 * 定义 RawData 的所有处理状态
 */
public enum ProcessingStatus {

    /**
     * 待处理
     */
    PENDING("待处理"),

    /**
     * 处理中
     */
    PROCESSING("处理中"),

    /**
     * 处理成功
     */
    SUCCESS("处理成功"),

    /**
     * 处理失败
     */
    FAILED("处理失败"),

    /**
     * 部分失败（部分数据处理成功）
     */
    PARTIALLY_FAILED("部分失败");

    /**
     * 显示名称
     */
    private final String displayName;

    private ProcessingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据字符串值查找枚举（用于 Jackson 反序列化）
     *
     * @param value 字符串值
     * @return 对应的枚举，如果找不到返回 null
     */
    @JsonCreator
    public static ProcessingStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 判断是否为终态（不会再变化的状态）
     * 
     * @return true 如果是终态
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == PARTIALLY_FAILED;
    }

    /**
     * 判断是否可以重试
     * 
     * @return true 如果可以重试
     */
    public boolean canRetry() {
        return this == FAILED || this == PARTIALLY_FAILED;
    }

    /**
     * 判断是否处理成功（包括部分成功）
     * 
     * @return true 如果处理成功或部分成功
     */
    public boolean isSuccessOrPartial() {
        return this == SUCCESS || this == PARTIALLY_FAILED;
    }

}
