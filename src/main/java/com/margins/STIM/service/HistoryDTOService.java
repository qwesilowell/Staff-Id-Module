/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.DTO.AccessHistoryDTO;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.enums.DevicePosition;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class HistoryDTOService {
    
    @Inject 
    private AccessLogService accessLogRepository;
    public List<AccessHistoryDTO> generateAccessHistory(LocalDate startDate, LocalDate endDate, Integer entranceId, String accessStatus) {
        List<AccessLog> logs = accessLogRepository.findLogsBetweenDates(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX), entranceId);

        Map<String, List<AccessLog>> groupedLogs = groupLogsByEmployeeEntranceDate(logs);

        List<AccessHistoryDTO> historyDTOs = new ArrayList<>();

        for (Map.Entry<String, List<AccessLog>> entry : groupedLogs.entrySet()) {
            List<AccessLog> logsPerGroup = entry.getValue();

            List<AccessLog> entries = logsPerGroup.stream()
                    .filter(log -> log.getDevice().getDevicePosition()== DevicePosition.ENTRY)
                    .filter(log -> "GRANTED".equals(log.getResult()))
                    .sorted(Comparator.comparing(AccessLog::getTimestamp))
                    .collect(Collectors.toList());

            List<AccessLog> exits = logsPerGroup.stream()
                    .filter(log -> log.getDevice().getDevicePosition()== DevicePosition.EXIT)
                    .filter(log -> "GRANTED".equals(log.getResult()))
                    .sorted(Comparator.comparing(AccessLog::getTimestamp))
                    .collect(Collectors.toList());

            historyDTOs.addAll(matchEntryExitPairs(entries, exits));
        }

        return filterByAccessStatus(historyDTOs, accessStatus);
    }
    
    public List<AccessHistoryDTO> findUnmatchedEntries(LocalDate startDate, LocalDate endDate, Integer entranceId) {
        List<AccessHistoryDTO> allHistory = generateAccessHistory(startDate, endDate, entranceId,"ALL");

        // Return only records with missing exits
        return allHistory.stream()
                .filter(dto -> dto.getTimeEntered() != null && dto.getTimeExited() == null)
                .collect(Collectors.toList());
    }
    
    private List<AccessHistoryDTO> filterByAccessStatus(List<AccessHistoryDTO> records, String accessStatus) {
        if (accessStatus == null || accessStatus.isEmpty() || "ALL".equals(accessStatus)) {
            return records;
        }

        return records.stream()
                .filter(record -> matchesStatus(record, accessStatus))
                .collect(Collectors.toList());
    }

    private boolean matchesStatus(AccessHistoryDTO record, String status) {
        switch (status) {
            case "MISSING_EXIT":
                return record.getTimeEntered() != null && record.getTimeExited() == null;
            case "MISSING_ENTRY":
                return record.getTimeEntered() == null && record.getTimeExited() != null;
            case "COMPLETE":
                return record.getTimeEntered() != null && record.getTimeExited() != null;
            default:
                return true;
        }
    }

    private Map<String, List<AccessLog>> groupLogsByEmployeeEntranceDate(List<AccessLog> logs) {
        return logs.stream()
                .filter(log -> log.getEmployee() != null && log.getDevice() != null && log.getDevice().getEntrance() != null)
                .collect(Collectors.groupingBy(this::createGroupingKey));
    }

    private String createGroupingKey(AccessLog log) {
        String employeeId = log.getEmployee().getGhanaCardNumber();
        int entranceId = log.getDevice().getEntrance().getId();
        String date = log.getTimestamp().toLocalDate().toString();
        return employeeId + "|" + entranceId + "|" + date;
    }

    private List<AccessHistoryDTO> matchEntryExitPairs(List<AccessLog> entries, List<AccessLog> exits) {
        List<AccessHistoryDTO> result = new ArrayList<>();
        List<AccessLog> unmatchedEntries = new ArrayList<>(entries);

        for (AccessLog exit : exits) {
            // Find the latest unmatched ENTRY before this EXIT
            AccessLog matchedEntry = null;

            for (int i = unmatchedEntries.size() - 1; i >= 0; i--) {
                AccessLog entry = unmatchedEntries.get(i);
                if (!exit.getTimestamp().isBefore(entry.getTimestamp())) {
                    matchedEntry = entry;
                    unmatchedEntries.remove(i); // removing so it won’t be reused
                    break;
                }
            }

            AccessHistoryDTO dto = new AccessHistoryDTO();
            dto.setTimeExited(exit.getTimestamp());
            dto.setDeviceExitName(exit.getDevice().getDeviceName());
            dto.setExitResult(exit.getResult());

            Employee emp = exit.getEmployee();
            dto.setGhanaCardNumber(emp.getGhanaCardNumber());
            dto.setEmployeeName(emp.getFullName());
            dto.setEntranceName(exit.getDevice().getEntrance().getEntranceName());
            dto.setDate(exit.getTimestamp().toLocalDate());

            if (matchedEntry != null) {
                dto.setTimeEntered(matchedEntry.getTimestamp());
                dto.setDeviceEntryName(matchedEntry.getDevice().getDeviceName());
                dto.setEntryResult(matchedEntry.getResult());
                if (matchedEntry.getId() != null) {
                    dto.setEntryLogId(matchedEntry.getId());
                }
            }
            result.add(dto);
        }

        // Make a  unmatched ENTRY logs that didn’t get paired
        for (AccessLog entry : unmatchedEntries) {
            AccessHistoryDTO dto = new AccessHistoryDTO();
            dto.setTimeEntered(entry.getTimestamp());
            dto.setDeviceEntryName(entry.getDevice().getDeviceName());
            dto.setEntryResult(entry.getResult());
            dto.setEntryLogId(entry.getId());

            Employee emp = entry.getEmployee();
            dto.setGhanaCardNumber(emp.getGhanaCardNumber());
            dto.setEmployeeName(emp.getFullName());
            dto.setEntranceName(entry.getDevice().getEntrance().getEntranceName());
            dto.setDate(entry.getTimestamp().toLocalDate());

            result.add(dto);
        }

        //sort result by timeEntered or timeExited
        result.sort(Comparator.comparing(dto
                -> dto.getTimeEntered() != null ? dto.getTimeEntered() : dto.getTimeExited(),
                Comparator.reverseOrder())
        );

        return result;
    }

}
