/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import com.margins.STIM.entity.AccessAnomaly;
import static com.margins.STIM.entity.enums.AnomalyType.STRICT_MODE_VIOLATION;
import static com.margins.STIM.entity.enums.AnomalyType.UNMATCHED_ENTRY;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author PhilipManteAsare
 */
public class AnomalyMessageBuilder {

    public static String buildTitle(AccessAnomaly anomaly) {
        switch (anomaly.getAnomalyType()) {
            case UNMATCHED_ENTRY:
                return "Unmatched Entry Detected";
            case FREQUENT_DENIED_ACCESS:
                return "Multiple Denied Access Attempts";
            case OUT_OF_TIME_RANGE_ENTRY:
                return "Access Outside Allowed Time";
            case STRICT_MODE_VIOLATION:
                return "Strict Mode Violation";
            case OUT_OF_TIME_RANGE_EXIT:
                    return"Exit Outside Allowed Time";
            default:
                return "Access Anomaly Detected";
        }
    }

    public static String buildDescription(AccessAnomaly anomaly) {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee: ").append(anomaly.getEmployee().getFullName());

        if (anomaly.getDevice() != null) {
            sb.append(" | Device: ").append(anomaly.getDevice().getDeviceName());
        }
        if (anomaly.getEntrance() != null) {
            sb.append(" | Entrance: ").append(anomaly.getEntrance().getEntranceName());
        }

        sb.append(" | Severity: ").append(anomaly.getAnomalySeverity());
        sb.append(" | Time: ").append(DateFormatter.forDateTimes(anomaly.getTimestamp()));

        if (anomaly.getMessage() != null && !anomaly.getMessage().isEmpty()) {
            sb.append(" | Details: ").append(anomaly.getMessage());
        }

        return sb.toString();
    }
}
