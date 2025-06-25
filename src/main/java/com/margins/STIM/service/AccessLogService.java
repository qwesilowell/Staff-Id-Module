/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.CustomTimeAccess;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;
import com.margins.STIM.util.DateFormatter;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class AccessLogService {

    @EJB
    private EntrancesService entrancesService;

    @Inject
    private TimeAccessRuleService timeAccessRuleService;

    @Inject
    private Employee_Service employeeService;

    @PersistenceContext
    private EntityManager em;

    public void logAccess(AccessLog log) {
        em.persist(log);
        em.flush();
        em.clear();
    }

    // For dashboard later
    public int countByResultInPeriod(String result, LocalDateTime start, LocalDateTime end) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM AccessLog a WHERE a.result = :result "
                + "AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("result", result)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }

    public int countByResultAndEntrance(String result, LocalDateTime start, LocalDateTime end, String entranceId) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM AccessLog a WHERE a.result = :result "
                + "AND a.entranceId = :entranceId AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("result", result)
                .setParameter("entranceId", entranceId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }

    public List<AccessLog> getRecentAccessAttempts(int limit) {
        return em.createQuery("SELECT a FROM AccessLog a ORDER BY a.timestamp DESC", AccessLog.class)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AccessLog> getALLAccessAttempts() {
        return em.createQuery("SELECT a FROM AccessLog a ORDER BY a.timestamp DESC", AccessLog.class)
                .getResultList();
    }

    public List<AccessLog> getRecentAccessAttemptsByUser(String ghanaCardNumber, int limit) {
        return em.createQuery("SELECT a FROM AccessLog a WHERE a.employee.ghanaCardNumber  = :ghanaCardNumber ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("ghanaCardNumber", ghanaCardNumber)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AccessLog> getRecentAccessAttemptsByEntrance(String entranceId, int limit) {
        return em.createQuery("SELECT a FROM AccessLog a WHERE a.entranceId = :entranceId ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("entranceId", entranceId)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AccessLog> getAllRecentAccessAttemptsByEntrance(String entranceId) {
        return em.createQuery("SELECT a FROM AccessLog a WHERE a.entranceId = :entranceId ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("entranceId", entranceId)
                .getResultList();
    }

    public Map<String, Integer> countAccessResultsByEntrance(LocalDateTime start, LocalDateTime end, String entranceId) {

        List<Object[]> results = em.createQuery(
                "SELECT a.result, COUNT(a) FROM AccessLog a "
                + "WHERE a.result IN ('granted', 'denied') "
                + "AND a.entranceId = :entranceId "
                + "GROUP BY a.result", Object[].class)
                .setParameter("entranceId", entranceId)
                .getResultList();

        Map<String, Integer> resultCounts = new HashMap<>();
        resultCounts.put("granted", 0);
        resultCounts.put("denied", 0);

        for (Object[] row : results) {
            String result = (String) row[0];
            Long count = (Long) row[1];
            if (result.equals("granted") || result.equals("denied")) {
                resultCounts.put(result, count.intValue());
            }
        }

        return resultCounts;
    }

    public Map<String, Integer> countAccessResultsForAllEntrances(LocalDateTime start, LocalDateTime end) {

        List<Object[]> results = em.createQuery(
                "SELECT a.result, COUNT(a) FROM AccessLog a "
                + "WHERE a.result IN ('granted', 'denied') "
                + "GROUP BY a.result", Object[].class)
                .getResultList();

        Map<String, Integer> resultCounts = new HashMap<>();
        resultCounts.put("granted", 0);
        resultCounts.put("denied", 0);

        for (Object[] row : results) {
            String result = (String) row[0];
            Long count = (Long) row[1];
            if (result.equals("granted") || result.equals("denied")) {
                resultCounts.put(result, count.intValue());
            }
        }

        return resultCounts;
    }

    public Map<String, Integer> getTop5RecentEntrancesByGrantedAccess(LocalDateTime start, LocalDateTime end) {

        List<Object[]> results = em.createQuery(
                "SELECT a.entranceId, COUNT(a) as accessCount, MAX(a.timestamp) as lastAccessTime "
                + "FROM AccessLog a "
                + "WHERE a.result = 'granted' AND a.timestamp BETWEEN :start AND :end "
                + "GROUP BY a.entranceId "
                + "ORDER BY MAX(a.timestamp) DESC", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setMaxResults(5)
                .getResultList();

        Map<String, Integer> entranceCounts = new HashMap<>();
        for (Object[] row : results) {
            String entranceId = (String) row[0];
            Long count = (Long) row[1];

            Entrances entrance = entrancesService.findEntranceById(entranceId);
            String entranceName = entrance != null ? entrance.getEntrance_Name() : entranceId;

            entranceCounts.put(entranceName, count.intValue());
        }

        return entranceCounts;
    }
    
    
    public Map<String, Map<String, Integer>> getTop5RecentEntrancesByAccessResult(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = em.createQuery(
                "SELECT a.entranceId, a.result, COUNT(a), MAX(a.timestamp) "
                + "FROM AccessLog a "
                + "WHERE a.timestamp BETWEEN :start AND :end "
                + "GROUP BY a.entranceId, a.result "
                + "ORDER BY MAX(a.timestamp) DESC", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        // Step 1: Count total accesses per entrance
        Map<String, LocalDateTime> entranceLastAccess = new HashMap<>();
        Map<String, Map<String, Integer>> accessMap = new HashMap<>();

        for (Object[] row : results) {
            String entranceId = (String) row[0];
            String result = (String) row[1]; // "granted" or "denied"
            Long count = (Long) row[2];
            LocalDateTime lastAccess = (LocalDateTime) row[3];

            // Track most recent access time
            entranceLastAccess.put(entranceId, lastAccess);

            // Group by entrance and result
            accessMap.computeIfAbsent(entranceId, k -> new HashMap<>());
            accessMap.get(entranceId).put(result, count.intValue());
        }

        // Step 2: Sort entrances by most recent access time
        List<String> top5EntranceIds = entranceLastAccess.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .distinct()
                .limit(5)
                .toList();

        // Step 3: Build final result map with entrance names
        Map<String, Map<String, Integer>> finalResult = new LinkedHashMap<>();
        for (String entranceId : top5EntranceIds) {
            Entrances entrance = entrancesService.findEntranceById(entranceId);
            String entranceName = entrance != null ? entrance.getEntrance_Name() : entranceId;
            finalResult.put(entranceName, accessMap.get(entranceId));
        }

        return finalResult;
    }
    


    private boolean isWithinRoleTimeRule(EmployeeRole role, LocalTime now, DayOfWeek day, Entrances entrance) {
        List<RoleTimeAccess> rules = timeAccessRuleService.findByRoleAndEntrance(role, entrance);

        for (RoleTimeAccess rule : rules) {
            if (rule.getDayOfWeek() == day) {
                LocalTime start = DateFormatter.toLocalTime(rule.getStartTime());
                LocalTime end = DateFormatter.toLocalTime(rule.getEndTime());

                if (!now.isBefore(start) && !now.isAfter(end)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isWithinCustomAccess(Employee emp, Entrances entrance, DayOfWeek today, LocalTime currentime) {
        List<CustomTimeAccess> customrules = timeAccessRuleService.findByEmployeeAndEntrance(emp, entrance);

        for (CustomTimeAccess rule : customrules) {
            if (rule.getDayOfWeek() == today) {
                LocalTime start = DateFormatter.toLocalTime(rule.getStartTime());
                LocalTime end = DateFormatter.toLocalTime(rule.getEndTime());

                if (!currentime.isBefore(start) && !currentime.isAfter(end))//if current time is NOT BEFORE start time and curremt time is NOT after end time 
                {
                    return true;
                }
                
               
            }
        }
        return false;
    }

    // New method to check access
    public boolean hasAccess(Employee employee, Entrances entrance) {
        //  Employee employee = employeeService.findEmployeeByGhanaCard(ghanaCardNumber);
//        Employee employee = em.find(Employee.class, ghanaCardNumber);
//        if (employee == null) {
//            System.out.println("Employee not found: " + ghanaCardNumber);
//            return false;
//        }
        if (employee == null) {
            System.out.println("employee is null");
            return false;
        }
        if (entrance == null) {
            System.out.println("entrance is null");
            return false;
        }
        

        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        // Check role-based entrances
        EmployeeRole role = employee.getRole();
        if (role != null && role.getAccessibleEntrances() != null) {
            boolean hasRoleAccess = role.getAccessibleEntrances()
                    .stream()
                    .anyMatch(e -> entrance.getEntrance_Device_ID().equals(e.getEntrance_Device_ID()));

            if (hasRoleAccess) {
                if (isWithinRoleTimeRule(role, now, today, entrance)) {
                    System.out.println("Allowed by default rolefor >>>> " + role);
                    System.out.println(today);
                    System.out.println(now);

                    return true; // ✅ Access granted via role + time
                }
            }
        }

        // Check custom entrances
        if (employee.getCustomEntrances() != null) {
            boolean hasCustomAccess = employee.getCustomEntrances()
                    .stream()
                    .anyMatch(e -> entrance.getEntrance_Device_ID().equals(e.getEntrance_Device_ID()));

            if (hasCustomAccess) {
                if (isWithinCustomAccess(employee, entrance, today, now)) {
                    System.out.println("Allowed by custom role for >>>> " + employee);
                    System.out.println(today);
                    System.out.println(now);
                    return true; // ✅ Access granted via custom rule
                }
            }
        }

        System.out.println("Access denied for employee>>>>>>: " + employee + " entrance>>>>>>>: " + entrance);
        return false;
    }

    public int countFilteredAccessLogs(String employeeName, String ghanaCardNumber, String entranceId,
            String result, LocalDateTime startTime, LocalDateTime endTime) {

        StringBuilder queryStr = new StringBuilder("SELECT COUNT(a) FROM AccessLog a WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (ghanaCardNumber != null && !ghanaCardNumber.isEmpty()) {
            queryStr.append(" AND a.employee.ghanaCardNumber LIKE :ghanaCardNumber");
            params.put("ghanaCardNumber", "%" + ghanaCardNumber + "%");
        }

        if (entranceId != null && !entranceId.isEmpty()) {
            queryStr.append(" AND a.entranceId = :entranceId");
            params.put("entranceId", entranceId);
        }

        if (result != null && !result.isEmpty()) {
            queryStr.append(" AND a.result = :result");
            params.put("result", result);
        }

        if (startTime != null && endTime != null) {
            queryStr.append(" AND a.timestamp BETWEEN :startTime AND :endTime");
            params.put("startTime", startTime);
            params.put("endTime", endTime);
        }

        TypedQuery<Long> query = em.createQuery(queryStr.toString(), Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult().intValue();
    }

    public List<AccessLog> filterAccessLog(List<LocalDateTime> dateRange, String entranceId, String ghanacardnumber, String employeeName,Integer roleId, String result) {

        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM AccessLog a WHERE 1=1 ");

        Map<String, Object> params = new HashMap<>();

        if (dateRange != null) {

            queryBuilder.append("AND a.timestamp BETWEEN :start AND :end ");

            params.put("start", dateRange.get(0));

            params.put("end", dateRange.get(1));

        }

        if (entranceId != null && !entranceId.isBlank()) {

            queryBuilder.append("AND a.entranceId = :entranceId ");

            params.put("entranceId", entranceId);

        }

        if (ghanacardnumber != null && !ghanacardnumber.isBlank()) {

            queryBuilder.append("AND UPPER(a.employee.ghanaCardNumber) LIKE :ghanacardnumber ");

            params.put("ghanacardnumber", "%" + ghanacardnumber.toUpperCase() + "%");

        }

        if (employeeName != null && !employeeName.isBlank()) {
            queryBuilder.append("AND (CONCAT(LOWER(a.employee.firstname), ' ', LOWER(a.employee.lastname)) LIKE :employeeName ");
            queryBuilder.append("OR LOWER(a.employee.firstname) LIKE :employeeName ");
            queryBuilder.append("OR LOWER(a.employee.lastname) LIKE :employeeName) ");
            
            params.put("employeeName", "%" + employeeName.toLowerCase() + "%");
        }
        
        if (roleId != null) {
            queryBuilder.append("AND a.employee.role.id = :roleId ");
            params.put("roleId", roleId);
        }

        if (result != null && !result.isBlank()) {

            queryBuilder.append("AND a.result = :result ");

            params.put("result", result);

        }

        queryBuilder.append("ORDER BY a.timestamp DESC");

        TypedQuery<AccessLog> query = em.createQuery(queryBuilder.toString(), AccessLog.class);

        params.forEach(query::setParameter);

        return query.getResultList();

    }

}
