package com.openclaw.observer.controller;

import com.openclaw.observer.common.util.ApiResponseHelper;
import com.openclaw.observer.document.RawDataDocument;
import com.openclaw.observer.service.RawDataProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RawData 监控和管理接口
 *
 * 提供以下功能：
 * 1. 查看 failed 的 RawData 列表
 * 2. 查看 pending 的 RawData 列表
 * 3. 手动重试单个 RawData
 * 4. 批量重试所有 failed 的 RawData
 * 5. 删除 RawData 及其子数据
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/raw-data")
@RequiredArgsConstructor
public class RawDataController {

    private final RawDataProcessorService rawDataProcessorService;

    // ==================== 查询接口 ====================

    /**
     * 获取所有 failed 的 RawData
     * http://localhost:10333/api/v1/raw-data/failed
     */
    @GetMapping("/failed")
    public ResponseEntity<List<RawDataDocument>> getFailedRawData() {
        log.info("查询 failed 的 RawData 列表");
        List<RawDataDocument> failedList = rawDataProcessorService.getFailedRawData();
        return ResponseEntity.ok(failedList);
    }

    /**
     * 获取所有 pending 的 RawData
     *
     */
    @GetMapping("/pending")
    public ResponseEntity<List<RawDataDocument>> getPendingRawData() {
        log.info("查询 pending 的 RawData 列表");
        List<RawDataDocument> pendingList = rawDataProcessorService.getPendingRawData();
        return ResponseEntity.ok(pendingList);
    }

    /**
     * 获取统计信息
     * http://localhost:10333/api/v1/raw-data/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("查询 RawData 统计信息");

        List<RawDataDocument> failedList = rawDataProcessorService.getFailedRawData();
        List<RawDataDocument> pendingList = rawDataProcessorService.getPendingRawData();

        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", pendingList.size());
        stats.put("failedCount", failedList.size());
        stats.put("failedList", failedList);
        stats.put("pendingList", pendingList);

        return ResponseEntity.ok(stats);
    }

    // ==================== 重试接口 ====================

    /**
     * 手动重试单个 RawData
     */
    @PostMapping("/{id}/retry")
    public ResponseEntity<RawDataDocument> retryRawData(@PathVariable String id) {
        log.info("手动重试 RawData: {}", id);
        try {
            RawDataDocument rawDoc = rawDataProcessorService.retryRawData(id);
            return ResponseEntity.ok(rawDoc);
        } catch (Exception e) {
            log.error("重试 RawData 失败 (id: {}): {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量重试所有 failed 的 RawData
     */
    @PostMapping("/retry-all")
    public ResponseEntity<Map<String, Object>> retryAllFailed() {
        log.info("批量重试所有 failed 的 RawData");
        try {
            int count = rawDataProcessorService.retryAllFailedRawData();
            return ResponseEntity.ok(ApiResponseHelper.successWithCount("已触发重试 " + count + " 条 RawData", count));
        } catch (Exception e) {
            log.error("批量重试失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseHelper.error(e.getMessage()));
        }
    }

    // ==================== 删除接口 ====================

    /**
     * 删除单个 RawData
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRawData(@PathVariable String id) {
        log.info("删除 RawData: {}", id);
        try {
            rawDataProcessorService.deleteRawData(id);
            return ResponseEntity.ok(ApiResponseHelper.success("已删除 RawData"));
        } catch (Exception e) {
            log.error("删除 RawData 失败 (id: {}): {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponseHelper.error(e.getMessage()));
        }
    }
}
