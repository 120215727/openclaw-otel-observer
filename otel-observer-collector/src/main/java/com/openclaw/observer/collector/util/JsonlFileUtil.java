package com.openclaw.observer.collector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class JsonlFileUtil {

    private static final String SHA_256 = "SHA-256";

    private final ObjectMapper objectMapper;

    public JsonlFileUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 计算文件前 N 行的 SHA-256 签名
     *
     * @param file      文件
     * @param lineCount 行数（从第 1 行开始）
     * @return SHA-256 签名的十六进制字符串，如果文件不存在或行数为 0 返回 null
     */
    public String calculateSha256ForLines(File file, long lineCount) {
        if (lineCount <= 0) {
            return null;
        }
        Path path = file.toPath();
        if (!Files.exists(path)) {
            return null;
        }

        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            List<String> lines = stream.limit(lineCount).collect(Collectors.toList());
            if (lines.isEmpty()) {
                return null;
            }
            // 使用换行符连接所有行，并在最后一行也加上换行符，保持与文件实际内容一致
            String content = String.join("\n", lines) + "\n";
            return calculateSha256(content);
        } catch (IOException e) {
            log.warn("计算文件 SHA 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 计算字符串的 SHA-256 签名
     */
    private String calculateSha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 从指定行开始读取新行
     *
     * @param file      文件
     * @param startLine 起始行（从 0 开始，返回 startLine 之后的行，不包含 startLine）
     * @return 新行列表
     */
    public List<String> readLinesFrom(File file, long startLine) {
        List<String> lines = new ArrayList<>();
        Path path = file.toPath();
        if (!Files.exists(path)) {
            return lines;
        }

        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            stream.skip(startLine).forEach(lines::add);
        } catch (IOException e) {
            log.error("读取文件行失败: {}", e.getMessage());
        }
        return lines;
    }

    /**
     * 验证一行是否是有效的 JSON
     *
     * @param line 行内容
     * @return 是否是有效的 JSON
     */
    public boolean isValidJson(String line) {
        if (line == null || line.trim().isEmpty()) {
            return false;
        }
        try {
            objectMapper.readTree(line);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 获取文件的总行数
     */
    public long getTotalLineCount(File file) {
        Path path = file.toPath();
        if (!Files.exists(path)) {
            return 0;
        }
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            return stream.count();
        } catch (IOException e) {
            log.warn("获取文件行数失败: {}", e.getMessage());
            return 0;
        }
    }

}
