/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.TimeAccessRule;
import com.margins.STIM.util.DateFormatter;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.primefaces.model.SortOrder;

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

    private boolean isWithinTimeRule(TimeAccessRule rule, LocalTime now, String todayCode) {
        if (rule == null || rule.getDaysOfWeek() == null) {
            return false;
        }

        List<String> allowedDays = Arrays.asList(rule.getDaysOfWeek().split(","));
        if (!allowedDays.contains(todayCode)) {
            return false; // ❌ Not allowed today
        }

        LocalTime start = DateFormatter.toLocalTime(rule.getStartTime());
        LocalTime end = DateFormatter.toLocalTime(rule.getEndTime());

        if (end.isAfter(start)) {
            return !now.isBefore(start) && !now.isAfter(end); // e.g. 08:00–18:00
        } else {
            // Overnight range, e.g. 20:00–06:00
            return !now.isBefore(start) || !now.isAfter(end);
        }
    }

    // New method to check access
    public boolean hasAccess(String ghanaCardNumber, String entranceDeviceId) {
        Employee employee = em.find(Employee.class, ghanaCardNumber);
        if (employee == null) {
            System.out.println("Employee not found: " + ghanaCardNumber);
            return false;
        }
        LocalTime now = LocalTime.now();
        String today = LocalDate.now().getDayOfWeek().name().substring(0, 3);

        // Check role-based entrances
        EmployeeRole role = employee.getRole();
        if (role != null && role.getAccessibleEntrances() != null) {
            boolean hasRoleAccess = role.getAccessibleEntrances()
                    .stream()
                    .anyMatch(e -> entranceDeviceId != null && entranceDeviceId.equals(e.getEntrance_Device_ID()));

            if (hasRoleAccess) {
                TimeAccessRule roleRule = timeAccessRuleService.getRuleByRoleAndEntrance(role.getId(), entranceDeviceId);
                if (roleRule != null && isWithinTimeRule(roleRule, now, today)) {
                    System.out.println("Allowed by default role");
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
                    .anyMatch(e -> entranceDeviceId.equals(e.getEntrance_Device_ID()));

            if (hasCustomAccess) {
                TimeAccessRule employeeRule = timeAccessRuleService.getRuleByEmployeeAndEntrance(employee.getGhanaCardNumber(), entranceDeviceId);
                if (employeeRule != null && isWithinTimeRule(employeeRule, now, today)) {
                    System.out.println("Allowed by custom role");
                    System.out.println(today);
                    System.out.println(now);
                    return true; // ✅ Access granted via custom rule
                }
            }
        }

        System.out.println("Access denied for employee: " + ghanaCardNumber + ", entrance: " + entranceDeviceId);
        return false;
    }

    public List<AccessLog> loadLazyAccessLogs(int first, int pageSize, String sortField, SortOrder sortOrder,
            Map<String, Object> filters, String employeeName, String ghanaCardNumber, String entranceId,
            String result, LocalDateTime startTime, LocalDateTime endTime) {

        StringBuilder queryStr = new StringBuilder("SELECT a FROM AccessLog a WHERE 1=1");
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

        queryStr.append(" ORDER BY a.timestamp DESC");

        TypedQuery<AccessLog> query = em.createQuery(queryStr.toString(), AccessLog.class);
        params.forEach(query::setParameter);

        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        return query.getResultList();
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

    public List<AccessLog> filterAccessLog(List<LocalDateTime> dateRange, String entranceId, String ghanacardnumber, String employeeName, String result) {

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

        if (ghanacardnumber != null&& !ghanacardnumber.isBlank()) {

            queryBuilder.append("AND a.employee.ghanaCardNumber LIKE :ghanacardnumber ");

            params.put("ghanacardnumber", "%" + ghanacardnumber.toUpperCase() + "%");

        }

        if (employeeName != null && !employeeName.isBlank()) {

            queryBuilder.append("AND (a.employee.firstname LIKE :employeeName OR a.employee.lastname LIKE :employeeName) " );

            params.put("employeeName", "%" + employeeName.toUpperCase() + "%");

        }
        
        if (result != null && !result.isBlank()) {

            queryBuilder.append("AND a.result = :result " );

            params.put("result", result);

        }

        queryBuilder.append("ORDER BY a.timestamp DESC");

        TypedQuery<AccessLog> query = em.createQuery(queryBuilder.toString(), AccessLog.class);

        params.forEach(query::setParameter);

        return query.getResultList();

    }

    
}
