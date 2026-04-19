package com.openclaw.observer.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 */
public class PageableHelper {

    private PageableHelper() {}

    /**
     * 创建按创建时间降序的 Pageable
     */
    public static Pageable createDescByCreatedAt(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /**
     * 创建按创建时间升序的 Pageable
     */
    public static Pageable createAscByCreatedAt(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    /**
     * 创建自定义排序的 Pageable
     */
    public static Pageable create(int page, int size, Sort.Direction direction, String sortField) {
        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    /**
     * 转换 Page 数据为列表
     */
    public static <T, R> List<R> convertPageToList(Page<T> page, Function<T, R> converter) {
        return page.getContent().stream()
            .map(converter)
            .collect(Collectors.toList());
    }
}
