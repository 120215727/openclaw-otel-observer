package com.openclaw.observer.repository;

import java.util.List;
import java.util.Map;

public interface CustomOtelMetricEsRepository {
    List<String> findDistinctMetricNames();
    Map<String, Long> getTopMetricCounts(int limit);
}
