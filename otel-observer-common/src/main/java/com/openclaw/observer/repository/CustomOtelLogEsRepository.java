package com.openclaw.observer.repository;

import java.util.Map;

public interface CustomOtelLogEsRepository {
    Map<String, Long> getLogLevelCounts();
}
