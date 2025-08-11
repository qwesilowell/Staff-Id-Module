/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.DTO.AccessHistoryDTO;
import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.AccessLog;
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

    public void detectUnmatchedEntriess() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(7);//change to 1 day


        List<AccessHistoryDTO> historyList = historyDTOService.generateAccessHistory(yesterday, today, null);
        
        LocalDateTime now = LocalDateTime.now();


        for (AccessHistoryDTO dto : historyList) {
            if (dto.getTimeEntered() != null && dto.getTimeExited() == null) {
                
                Duration sinceEntry = Duration.between(dto.getTimeEntered(), now);
                Long hours = sinceEntry.toHours();
                
                if (hours >= 12) {
                    AccessLog entryLog = accessLogService.findAccessLogById(dto.getEntryLogId());
                    if(entryLog == null){
                        continue;
                    }
                    if ( !accessLogService.existsFor(entryLog, AnomalyType.UNMATCHED_ENTRY)){
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
                    }
                }  
            }
        }
    }
}
