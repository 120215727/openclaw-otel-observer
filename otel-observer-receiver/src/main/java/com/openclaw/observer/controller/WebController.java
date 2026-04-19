package com.openclaw.observer.controller;

import com.openclaw.observer.document.*;
import com.openclaw.observer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebController {

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/traces")
    public String traces(Model model) {
        return "traces";
    }

    @GetMapping("/metrics")
    public String metrics(Model model) {
        return "metrics";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        return "logs";
    }

    @GetMapping("/sessions")
    public String sessions(Model model) {
        return "sessions";
    }

    @GetMapping("/sessions/{sessionId}")
    public String sessionDetail(@PathVariable String sessionId, Model model) {
        model.addAttribute("sessionId", sessionId);
        return "session-detail";
    }
}
