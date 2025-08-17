/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.enums;

/**
 *
 * @author PhilipManteAsare
 */

public enum AnomalyType {
    UNMATCHED_ENTRY(AnomalySeverity.WARNING),
    UNMATCHED_EXIT(AnomalySeverity.INFO),
    OUT_OF_TIME_RANGE_ENTRY(AnomalySeverity.WARNING),
    OUT_OF_TIME_RANGE_EXIT(AnomalySeverity.WARNING),
    STRICT_MODE_VIOLATION(AnomalySeverity.SEVERE),
    FREQUENT_DENIED_ACCESS(AnomalySeverity.SEVERE);
    
    private final AnomalySeverity severity;

    AnomalyType(AnomalySeverity severity) {
        this.severity = severity;
    }

    public AnomalySeverity getSeverity() {
        return severity;
    }
}

//UNMATCHED_EXIT
//Used when a person badges out but no entry log was ever found.
//
//Implies the person may have tailgated or bypassed the system.
//
// UNMATCHED_ENTRY
//A person badges in but never badged out (even after many hours).
//
//You can set this to trigger after 12 hours (as earlier said).
//
//OUT_OF_TIME_RANGE_ENTRY
//Entry was attempted when not allowed based on time rule.
//
//In STRICT or LENIENT, this can be a soft or hard block.
//
//OUT_OF_TIME_RANGE_EXIT
//Exit happened outside of allowed time, but was still granted.
//
//Should always log as anomaly but not block access.
//
//STRICT_MODE_VIOLATION
//State mismatch (e.g., trying to enter when already marked INSIDE).
//
//Use this to track wrong sequence (entry-entry or exit-exit) in strict mode.
//
//FREQUENT_DENIED_ACCESS
//Multiple denied attempts (e.g. 3+ within 2 minute).
//
//Could indicate misuse or faulty badge.


//Key Features of This Implementation:
//
//Real-time Detection: Checks immediately after each denied attempt
//Time Window: 3+ denials within 2 minutes (configurable)
//Anti-Spam: Prevents duplicate anomaly logging for the same pattern
//Employee+Device Specific: Tracks patterns per employee-device combination
//Extensible: Easy to add badge locking or security alerts