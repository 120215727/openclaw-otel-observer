package com.openclaw.observer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogLevelDistributionResponse {
    private long info;
    private long warn;
    private long error;
    private long debug;

    public void increment(String level) {
        if (level == null) return;
        switch (level.toUpperCase()) {
            case "INFO" -> info++;
            case "WARN" -> warn++;
            case "ERROR" -> error++;
            case "DEBUG" -> debug++;
        }
    }
}
