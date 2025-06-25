/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.util.EntranceReportDTO;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
public class ReportsService {

    @PersistenceContext
    private EntityManager em;

    @EJB
    private EntrancesService entranceService;
    
    public List<Employee> filterEmployees(String employeeName, String ghanaCardNumber, EmployeeRole role, List<LocalDateTime> timeRange) {
        StringBuilder queryBuilder = new StringBuilder("SELECT e FROM Employee e WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (employeeName != null && !employeeName.isBlank()) {
            queryBuilder.append("AND (CONCAT(LOWER(e.employee.firstname), ' ', LOWER(e.employee.lastname)) LIKE :employeeName ");
            queryBuilder.append("OR LOWER(e.employee.firstname) LIKE :employeeName ");
            queryBuilder.append("OR LOWER(e.employee.lastname) LIKE :employeeName) ");
            params.put("employeeName", "%" + employeeName.toUpperCase() + "%");
        }

        if (ghanaCardNumber != null && !ghanaCardNumber.isBlank()) {
            queryBuilder.append("AND UPPER(e.ghanaCardNumber) LIKE :ghanaCardNumber ");
            params.put("ghanaCardNumber", "%" + ghanaCardNumber.toUpperCase() + "%");
        }

        if (role != null) {
            queryBuilder.append("AND e.role = :role ");
            params.put("role", role);
        }

        if (timeRange != null && timeRange.size() == 2) {
            LocalDateTime start = timeRange.get(0);
            LocalDateTime end = timeRange.get(1);

            Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());

            Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());

            queryBuilder.append("AND e.createdAt BETWEEN :startDate AND :endDate ");
            params.put("startDate", startDate);
            params.put("endDate", endDate);
        }

        queryBuilder.append("ORDER BY e.createdAt DESC");

        TypedQuery<Employee> query = em.createQuery(queryBuilder.toString(), Employee.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }
        
    public List<EntranceReportDTO> getEntranceReport1(LocalDateTime startDate, LocalDateTime endDate, String entranceId) {
        List<AccessLog> logs = em.createQuery("SELECT e FROM AccessLog e WHERE e.timestamp BETWEEN :start AND :end", AccessLog.class)
                .setParameter("start", startDate)
                .setParameter("end", endDate)
                .getResultList();

        if (entranceId != null && !entranceId.isEmpty()) {
            logs = logs.stream()
                    .filter(log -> entranceId.equals(log.getEntranceId()))
                    .collect(Collectors.toList());
        }
        //logs.stream().filter(log -> log.getEntranceId() != null log.getEntranceId());
        Map<String, Integer> entranceLogs = new HashMap<>();
        Map<String, Integer> resultLogs = new HashMap<>();
        
        Map<String, Map<String, Integer>> entranceSpecificLogs = new HashMap<>();
        
        for (AccessLog log : logs) {
            
            
            String entranceSpec = log.getEntranceId();
            if (entranceSpec!= null) {  
                Map<String, Integer> data = entranceSpecificLogs.getOrDefault(entranceSpec, new HashMap<String, Integer>());
                
                if (log.getEntranceId().equals(entranceSpec)) {
                    String resultType = log.getResult();
                    if (resultType != null) {
                        int count = data.getOrDefault(resultType, 0);
                        data.put(resultType, count + 1);
                    }
                    entranceSpecificLogs.put(entranceSpec, data);
                    
                    
                }
               
            }
            
            String currentEntrance = log.getEntranceId();
            if (currentEntrance!= null) {  
                int count = entranceLogs.getOrDefault(currentEntrance, 0);
                entranceLogs.put(currentEntrance, count + 1);
            }
            
            String resultType = log.getResult();
            if (resultType != null) {
                int count = resultLogs.getOrDefault(resultType, 0);
                resultLogs.put(resultType, count + 1);
            }

        }
        
        System.out.println("entrance maps >>>>>>>>>>>>> " + entranceLogs.toString());
        System.out.println("results maps >>>>>>>>>>>>> " + resultLogs.toString());
        System.out.println("specific maps >>>>>>>>>>>>> " + entranceSpecificLogs.toString());
        
        EntranceReportDTO dto = new EntranceReportDTO();
        dto.setDeniedAccesses(resultLogs.get("denied"));
        dto.setGrantedAccesses(resultLogs.get("granted"));
        dto.setTotalAccesses(0);
        
        
        
        return Arrays.asList(dto);
    }
    

    public List<EntranceReportDTO> getEntranceReport(LocalDateTime startDate, LocalDateTime endDate, String entranceId) {
        StringBuilder jpql = new StringBuilder("SELECT e FROM AccessLog e WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (entranceId != null && !entranceId.isEmpty()) {
            jpql.append(" AND e.entranceId = :entranceId");
            params.put("entranceId", entranceId);
        }

        if (startDate != null && endDate != null) {
            jpql.append(" AND e.timestamp BETWEEN :startDate AND :endDate");
            params.put("startDate", startDate);
            params.put("endDate", endDate);
        }

        jpql.append(" ORDER BY e.timestamp DESC");

        TypedQuery<AccessLog> query = em.createQuery(jpql.toString(), AccessLog.class);
        params.forEach(query::setParameter);

        List<AccessLog> logs = query.getResultList();
        if (logs.isEmpty()) {
            return Collections.emptyList();
        }

        // Group logs by entranceId
        Map<String, List<AccessLog>> logsByEntrance = logs.stream()
                .collect(Collectors.groupingBy(AccessLog::getEntranceId));

        List<EntranceReportDTO> reports = new ArrayList<>();

        for (Map.Entry<String, List<AccessLog>> entry : logsByEntrance.entrySet()) {
            String currentEntranceId = entry.getKey();
            List<AccessLog> entranceLogs = entry.getValue();

            Entrances entrance = entranceService.findEntranceById(currentEntranceId);
            if (entrance == null) {
                continue;
            }

            EntranceReportDTO dto = new EntranceReportDTO();
            dto.setEntrances(entrance);

            dto.setTotalAccesses(entranceLogs.size());

            dto.setGrantedAccesses(entranceLogs.stream()
                    .filter(log -> "granted".equalsIgnoreCase(log.getResult()))
                    .count());

            dto.setDeniedAccesses(entranceLogs.stream()
                    .filter(log -> "denied".equalsIgnoreCase(log.getResult()))
                    .count());

            dto.setUniqueUsers(entranceLogs.stream()
                    .map(log -> log.getEmployee() != null ? log.getEmployee().getGhanaCardNumber() : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .count());

            Optional<AccessLog> lastAccess = entranceLogs.stream()
                    .max(Comparator.comparing(AccessLog::getTimestamp));

            if (lastAccess.isPresent()) {
                dto.setLastAccessed(lastAccess.get().getTimestamp());

                Employee lastEmp = lastAccess.get().getEmployee();
                dto.setLastAccessedBy(lastEmp != null ? lastEmp.getFullName() : "Unknown");
                dto.setLastAccessStatus(lastAccess.get().getResult());
            }

            dto.setEmployeeCount(entrance.getEmployees() != null ? entrance.getEmployees().size() : 0);
            dto.setRoleCount(entrance.getAllowedRoles() != null ? entrance.getAllowedRoles().size() : 0);

            reports.add(dto);
        }

        return reports;
    }

    
    private EntranceReportDTO createReport(Entrances entrance, List<AccessLog> logs) {
        EntranceReportDTO dto = new EntranceReportDTO();
        dto.setEntrances(entrance);
        dto.setTotalAccesses(logs.size());

        Set<String> uniqueUserIds = new HashSet<>();
        long granted = 0;
        long denied = 0;

        LocalDateTime latestTime = null;
        AccessLog lastLog = null;

        for (AccessLog log : logs) {
            if (log.getResult() != null && log.getResult().equalsIgnoreCase("granted")) {
                granted++;
            } else if (log.getResult() != null && log.getResult().equalsIgnoreCase("denied")) {
                denied++;
            }

            if (log.getEmployee() != null) {
                uniqueUserIds.add(log.getEmployee().getGhanaCardNumber());
            }

            if (latestTime == null || (log.getTimestamp() != null && log.getTimestamp().isAfter(latestTime))) {
                latestTime = log.getTimestamp();
                lastLog = log;
            }
        }

        dto.setGrantedAccesses(granted);
        dto.setDeniedAccesses(denied);
        dto.setUniqueUsers(uniqueUserIds.size());

        if (lastLog != null) {
            dto.setLastAccessed(lastLog.getTimestamp());
            if (lastLog.getEmployee() != null) {
                dto.setLastAccessedBy(lastLog.getEmployee().getFullName());
            }
            dto.setLastAccessStatus(lastLog.getResult());
        }

        dto.setEmployeeCount(entrance.getEmployees() != null ? entrance.getEmployees().size() : 0);
        dto.setRoleCount(entrance.getAllowedRoles() != null ? entrance.getAllowedRoles().size() : 0);

        return dto;
    }


//    public List<EntranceReportDTO> getEntranceReportWithAverage(LocalDateTime startDate,
//            LocalDateTime endDate,
//            String entranceId) {
//        List<EntranceReportDTO> reports = getEntranceReport(startDate, endDate, entranceId);
//
//        // Calculate average access per unique user
//        reports.forEach(report -> {
//            if (report.getUniqueUsers() > 0) {
//                double avgAccess = (double) report.getTotalAccesses() / report.getUniqueUsers();
//                // You can add this to your DTO if needed
//                // report.setAvgAccess(avgAccess);
//            }
//        });
//
//        return reports;
//    }
}
