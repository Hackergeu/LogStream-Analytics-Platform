package com.logstream.backend.alerting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertRule {

    private String name;
    private String query;
    private long thresholdCount;
    private long windowMinutes;
}