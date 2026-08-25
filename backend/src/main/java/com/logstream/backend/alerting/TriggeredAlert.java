package com.logstream.backend.alerting;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TriggeredAlert {
    private String ruleName;
    private String query;
    private long matchCount;
    private long threshold;
    private long triggeredAt;
}