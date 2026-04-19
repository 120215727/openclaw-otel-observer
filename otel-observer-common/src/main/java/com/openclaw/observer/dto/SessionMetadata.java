package com.openclaw.observer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Session Metadata DTO - 对应 sessions.json 中的单个 session 记录
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionMetadata {

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("updatedAt")
    private Long updatedAt;

    @JsonProperty("systemSent")
    private Boolean systemSent;

    @JsonProperty("abortedLastRun")
    private Boolean abortedLastRun;

    @JsonProperty("chatType")
    private String chatType;

    @JsonProperty("deliveryContext")
    private DeliveryContext deliveryContext;

    @JsonProperty("lastChannel")
    private String lastChannel;

    @JsonProperty("origin")
    private Origin origin;

    @JsonProperty("sessionFile")
    private String sessionFile;

    @JsonProperty("compactionCount")
    private Integer compactionCount;

    @JsonProperty("skills")
    private List<SkillInfo> skills;

    @JsonProperty("modelProvider")
    private String modelProvider;

    @JsonProperty("model")
    private String model;

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("cwd")
    private String cwd;

    @JsonProperty("skillsSnapshot")
    private SkillsSnapshot skillsSnapshot;

    @JsonProperty("systemPromptReport")
    private SystemPromptReport systemPromptReport;

    @JsonProperty("runtimeMs")
    private Long runtimeMs;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeliveryContext {
        @JsonProperty("channel")
        private String channel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Origin {
        @JsonProperty("label")
        private String label;

        @JsonProperty("provider")
        private String provider;

        @JsonProperty("surface")
        private String surface;

        @JsonProperty("chatType")
        private String chatType;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillInfo {
        @JsonProperty("name")
        private String name;

        @JsonProperty("requiredEnv")
        private List<String> requiredEnv;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillsSnapshot {
        @JsonProperty("skills")
        private List<SkillInfo> skills;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SystemPromptReport {
        @JsonProperty("sessionKey")
        private String sessionKey;

        @JsonProperty("workspaceDir")
        private String workspaceDir;

        @JsonProperty("sandbox")
        private Sandbox sandbox;

        @JsonProperty("injectedWorkspaceFiles")
        private List<InjectedWorkspaceFile> injectedWorkspaceFiles;

        @JsonProperty("skills")
        private Skills skills;

        @JsonProperty("tools")
        private Tools tools;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sandbox {
        @JsonProperty("mode")
        private String mode;

        @JsonProperty("sandboxed")
        private Boolean sandboxed;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InjectedWorkspaceFile {
        @JsonProperty("name")
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Skills {
        @JsonProperty("entries")
        private List<SkillEntry> entries;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillEntry {
        @JsonProperty("name")
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Tools {
        @JsonProperty("entries")
        private List<ToolEntry> entries;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ToolEntry {
        @JsonProperty("name")
        private String name;
    }
}
