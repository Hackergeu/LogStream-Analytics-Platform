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

    // Thread-safe list for scheduler reads and REST API writes
    private final List<AlertRule> rules = new CopyOnWriteArrayList<>();

    public void addRule(AlertRule rule) {
        rules.add(rule);
        log.info("Registered alert rule: {}", rule.getName());
    }

    public List<AlertRule> getRules() {
        return rules;
    }

    @Scheduled(fixedRate = 60000) // Runs every 60 seconds
    public void evaluateRules() {

        if (rules.isEmpty()) {
            return;
        }

        for (AlertRule rule : rules) {
            try {
                long windowMillis = rule.getWindowMinutes() * 60 * 1000;

                long count = logSearchService.countMatchingInWindow(
                        rule.getQuery(),
                        windowMillis
                );

                if (count >= rule.getThresholdCount()) {
                    triggerAlert(rule, count);
                }

            } catch (Exception e) {
                log.error(
                        "Failed to evaluate alert rule '{}'",
                        rule.getName(),
                        e
                );
            }
        }
    }

    private void triggerAlert(AlertRule rule, long actualCount) {

        // Currently simulating an alert by writing it to the application logs.
        // Later, this can be replaced with Slack/PagerDuty/webhook integration.
        log.warn(
                "🚨 ALERT TRIGGERED: '{}' — {} matches (threshold: {}) for query [{}]",
                rule.getName(),
                actualCount,
                rule.getThresholdCount(),
                rule.getQuery()
        );
    }
}
