package com.logstream.backend.controller;

import com.logstream.backend.grpc.LogMessage;
import com.logstream.backend.search.LogIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogIngestController {

    private final LogIndexService logIndexService;

    @PostMapping("/ingest")
    public Map<String, Object> ingest(@RequestBody Map<String, String> body) throws Exception {
        LogMessage logMessage = LogMessage.newBuilder()
                .setLogId(UUID.randomUUID().toString())
                .setTimestamp(System.currentTimeMillis())
                .setLevel(body.getOrDefault("level", "INFO"))
                .setService(body.getOrDefault("service", "unknown"))
                .setMessage(body.getOrDefault("message", ""))
                .setResponseTimeMs(Integer.parseInt(body.getOrDefault("responseTimeMs", "0")))
                .build();

        logIndexService.indexLog(logMessage);

        return Map.of("success", true, "log_id", logMessage.getLogId());
    }
}