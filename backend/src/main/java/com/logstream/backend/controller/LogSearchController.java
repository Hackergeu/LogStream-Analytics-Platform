package com.logstream.backend.controller;

import com.logstream.backend.search.LogSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
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

    @GetMapping("/api/logs/timeseries")
    public Map<Long, Long> getTimeSeries(
            @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) throws Exception {
        long toTime = (to != null) ? to : System.currentTimeMillis();
        long fromTime = (from != null) ? from : toTime - (60 * 60 * 1000); // default: last hour
        return logSearchService.getLogCountsByMinute(fromTime, toTime);
    }
}