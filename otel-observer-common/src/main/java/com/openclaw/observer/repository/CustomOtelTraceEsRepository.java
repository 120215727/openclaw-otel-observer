package com.openclaw.observer.repository;

import java.util.List;

public interface CustomOtelTraceEsRepository {
    List<String> findDistinctServiceNames();
}
