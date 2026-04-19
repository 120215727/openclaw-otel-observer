package com.openclaw.observer.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * API 响应工具类
 */
public class ApiResponseHelper {

    private ApiResponseHelper() {}

    /**
     * 创建成功响应
     */
    public static Map<String, Object> success(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        return result;
    }

    /**
     * 创建成功响应（带数据）
     */
    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    /**
     * 创建成功响应（带计数）
     */
    public static Map<String, Object> successWithCount(String message, int count) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        result.put("retriedCount", count);
        return result;
    }

    /**
     * 创建错误响应
     */
    public static Map<String, Object> error(String errorMessage) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", errorMessage);
        return result;
    }
}
