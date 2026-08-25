package com.logstream.backend.controller;

import com.logstream.backend.alerting.AlertRule;
import com.logstream.backend.alerting.AlertingService;
import com.logstream.backend.alerting.TriggeredAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertingService alertingService;

    @PostMapping
    public AlertRule createRule(@RequestBody AlertRule rule) {
        alertingService.addRule(rule);
        return rule;
    }

    @GetMapping
    public List<AlertRule> listRules() {
        return alertingService.getRules();
    }

    @GetMapping("/triggered")
    public List<TriggeredAlert> getTriggeredAlerts() {
        return alertingService.getTriggeredAlerts();
    }
}