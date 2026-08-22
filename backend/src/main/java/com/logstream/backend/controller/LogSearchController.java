package com.logstream.backend.controller;

import com.logstream.backend.search.LogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LogSearchController {

    private final LogSearchService logSearchService;

    @GetMapping("/api/logs/search")
    public List<Map<String, String>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "50") int limit) throws Exception {
        return logSearchService.search(q, limit);
    }
}