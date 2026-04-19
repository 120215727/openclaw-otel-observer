package com.openclaw.observer.common;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 公共常量类
 */
public class ObserverConstants {

    private ObserverConstants() {}

    /**
     * 上海时区
     */
    public static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /**
     * ES 日期格式
     */
    public static final DateTimeFormatter ES_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 文件名日期格式
     */
    public static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
