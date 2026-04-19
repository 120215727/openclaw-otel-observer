package com.openclaw.observer.collector.util;

import com.openclaw.observer.common.enums.SessionFileType;

import java.io.File;

public class FileTypeDetector {

    public static SessionFileType detect(File file) {
        String filename = file.getName();

        if ("sessions.json".equals(filename)) {
            return SessionFileType.SESSIONS_JSON;
        }

        if (filename.endsWith(".jsonl")) {
            if (filename.contains(".reset.")) {
                return SessionFileType.RESET_BACKUP;
            }
            if (filename.contains(".deleted.")) {
                return SessionFileType.DELETED_BACKUP;
            }
            return SessionFileType.SESSION_JSONL;
        }

        return SessionFileType.UNKNOWN;
    }

    public static String extractSessionId(String filename) {
        if (filename.contains(".reset.")) {
            return filename.split("\\.reset\\.")[0];
        }
        if (filename.contains(".deleted.")) {
            return filename.split("\\.deleted\\.")[0];
        }
        return filename.replace(".jsonl", "");
    }

    public static String extractFileTimestamp(String filename) {
        if (filename.contains(".reset.")) {
            // e237d05b-1a98-4809-8856-4eeeaeb154cc.jsonl.reset.2026-03-27T15-08-28.791Z
            return filename.substring(filename.indexOf(".reset.") + 7);
        }
        if (filename.contains(".deleted.")) {
            return filename.substring(filename.indexOf(".deleted.") + 9);
        }
        return null;
    }
}
