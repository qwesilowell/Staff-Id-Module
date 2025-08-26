/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.DTO.AccessHistoryDTO;
import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.enums.AnomalyType;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AnomalyDetectionService {

    @Inject
    private HistoryDTOService historyDTOService;

    @Inject
    private AccessLogService accessLogService;

    @Inject
    private NotificationService notification;

    @Inject
    private AnomalyRefGeneratorService anomalyRefGen;

    public void detectUnmatchedEntriess() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(7);//change to 1 day

        List<AccessHistoryDTO> unmatchedEntries = historyDTOService.findUnmatchedEntries(yesterday, today, null);

        LocalDateTime now = LocalDateTime.now();

        for (AccessHistoryDTO dto : unmatchedEntries) {
            Duration sinceEntry = Duration.between(dto.getTimeEntered(), now);
            Long hours = sinceEntry.toHours();

            if (hours >= 12) {
                AccessLog entryLog = accessLogService.findAccessLogById(dto.getEntryLogId());
                if (entryLog == null) {
                    continue;
                }
                if (!accessLogService.existsFor(entryLog, AnomalyType.UNMATCHED_ENTRY)) {
                    AccessAnomaly anomaly = new AccessAnomaly();
                    anomaly.setAnomalyType(AnomalyType.UNMATCHED_ENTRY);
                    anomaly.setAnomalySeverity(anomaly.getAnomalyType().getSeverity());
                    anomaly.setEmployee(entryLog.getEmployee());
                    anomaly.setMessage("Hasn't exited for over " + hours + " hours");
                    anomaly.setDevice(entryLog.getDevice());
                    anomaly.setEntrance(entryLog.getDevice().getEntrance());
                    anomaly.setTimestamp(now);
                    anomaly.setAccessLog(entryLog);

                    accessLogService.logAnomalies(anomaly);

//                    notification.notifyAllAdminsofAnomaly(anomaly);
                }
            }
        }
    }

    /**
     * Detects frequent denied access attempts within a time window Call this
     * after each denied access attempt
     *
     * @param employee
     * @param device
     */
    public void checkFrequentDeniedAccess(Employee employee, Devices device) {
        System.out.println("Checking Frequent Access>>>>>>>>>>");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(3); // 3-minute window

        // Count failed attempts in the last 2 minutes for this employee+device combo
        Long recentDenials = accessLogService.countDeniedAttemptsInTimeWindow(
                employee.getId(),
                device.getId(),
                windowStart,
                now
        );

        if (recentDenials >= 3) { // 3+ denials in 1 minutes
            // Check if we've already logged this anomaly recently (avoid spam)
            if (!accessLogService.hasRecentFrequentDenialAnomaly(employee.getId(), device.getId(), windowStart)) {

                AccessAnomaly anomaly = new AccessAnomaly();
                anomaly.setAnomalyType(AnomalyType.FREQUENT_DENIED_ACCESS);
                anomaly.setAnomalySeverity(anomaly.getAnomalyType().getSeverity());
                anomaly.setEmployee(employee);
                anomaly.setMessage("Multiple denied attempts (" + recentDenials + ") within 3 minutes");
                anomaly.setDevice(device);
                anomaly.setEntrance(device.getEntrance());
                anomaly.setTimestamp(now);
                //I Don't set accessLog since this spans multiple attempts

                accessLogService.logAnomalies(anomaly);
//                notification.notifyAllAdminsofAnomaly(anomaly);

                // Optional: Temporarily lock the badge or alert security
                // securityService.temporaryLockBadge(employee, Duration.ofMinutes(5));
            }
        }
    }
}

//What This Service Does
//
//Someone just got denied access
//"Let me check - how many times has this person been denied in the last 2 minutes?"
//If 3 or more times: "This looks suspicious, let me create an alert"
//But only create ONE alert per incident (don't spam)
//
//Real-World Example:
//Scenario 1:
//
//John badges in Monday at 9 AM
//Tuesday at 10 AM, system runs the check
//"John has been inside for 25 hours without leaving - that's suspicious!"
//Creates alert: "John hasn't exited for over 25 hours"
//
//Scenario 2:
//
//Sarah tries her badge at 2:00 PM - DENIED
//Sarah tries again at 2:01 PM - DENIED
//Sarah tries again at 2:01:30 PM - DENIED
//System thinks: "3 denials in 90 seconds - something's wrong!"
//Creates alert: "Multiple denied attempts (3) within 2 minutes"
//
//The Smart Parts:
//
//No spam: Won't create duplicate alerts for the same issue
//Automatic: Runs without human intervention
//Detailed: Records exactly what happened and when
//Flexible: Can be adjusted (change 12 hours to 8 hours, change 3 attempts to 5, etc.)
//
//Think of it as having a really observant security guard who never sleeps and remembers everything!
