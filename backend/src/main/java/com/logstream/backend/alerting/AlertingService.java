package com.logstream.backend.alerting;

import com.logstream.backend.search.LogSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertingService {

    private final LogSearchService logSearchService;

    private final List<AlertRule> rules = new CopyOnWriteArrayList<>();
    private final List<TriggeredAlert> triggeredAlerts = new CopyOnWriteArrayList<>();
    private static final int MAX_TRIGGERED_HISTORY = 50;

    public void addRule(AlertRule rule) {
        rules.add(rule);
        log.info("Registered alert rule: {}", rule.getName());
    }

    public List<AlertRule> getRules() {
        return rules;
    }

    public List<TriggeredAlert> getTriggeredAlerts() {
        return triggeredAlerts;
    }

    @Scheduled(fixedRate = 60000)
    public void evaluateRules() {
        if (rules.isEmpty()) return;

        for (AlertRule rule : rules) {
            try {
                long windowMillis = rule.getWindowMinutes() * 60 * 1000;
                long count = logSearchService.countMatchingInWindow(rule.getQuery(), windowMillis);

                if (count >= rule.getThresholdCount()) {
                    triggerAlert(rule, count);
                }
            } catch (Exception e) {
                log.error("Failed to evaluate alert rule '{}'", rule.getName(), e);
            }
        }
    }

    private void triggerAlert(AlertRule rule, long actualCount) {
        log.warn("🚨 ALERT TRIGGERED: '{}' — {} matches (threshold: {}) for query [{}]",
                rule.getName(), actualCount, rule.getThresholdCount(), rule.getQuery());

        TriggeredAlert event = new TriggeredAlert(
                rule.getName(), rule.getQuery(), actualCount, rule.getThresholdCount(), System.currentTimeMillis()
        );
        triggeredAlerts.add(0, event); // newest first

        // keep the list bounded so it doesn't grow forever
        while (triggeredAlerts.size() > MAX_TRIGGERED_HISTORY) {
            triggeredAlerts.remove(triggeredAlerts.size() - 1);
        }
    }
}