/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.DTO.EntranceAccessStatsDTO;
import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.CustomTimeAccess;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;
import com.margins.STIM.entity.enums.AnomalyType;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.entity.enums.EntranceMode;
import com.margins.STIM.entity.enums.LocationState;
import com.margins.STIM.entity.model.AccessEvaluationResult;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.JSF;
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
import java.util.stream.Collectors;

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

    @Inject
    private EmployeeEntranceStateService employeeEntranceStateService;

    @Inject
    private AnomalyRefGeneratorService anomalyRefGen;

    @Inject
    private NotificationService notification;

    @PersistenceContext
    private EntityManager em;

    public void logAccess(AccessLog log) {
        em.persist(log);
        em.flush();
        em.clear();
    }

    public void logAnomalies(AccessAnomaly log) {
        log.setAnomalyRef(anomalyRefGen.generateUniqueAnomalyRef());
        em.persist(log);
        em.flush();
        notifyAdmin(log);
        
        em.clear();
        //Consider batch processing to flush and clear 
    }

    private void notifyAdmin(AccessAnomaly log) {
        try {
            AccessAnomaly aa = anomalyRefGen.findByAnomalyRef(log.getAnomalyRef());
            notification.notifyAllAdminsofAnomaly(aa);
            JSF.addWarningMessageWithSummary("Notify!","New Anomaly Found");
        } catch (Exception e) {
            e.printStackTrace();
            JSF.addErrorMessage("Failed To Notify ");
        }
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
                + "AND a.device.entrance.entranceDeviceId = :entranceId AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("result", result)
                .setParameter("entranceId", entranceId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }

    public AccessLog findAccessLogById(long id) {
        return em.find(AccessLog.class, id);
    }

    public List<AccessAnomaly> findAnomalyByAccessLog(AccessLog log) {
        return em.createQuery("SELECT a FROM AccessAnomaly a WHERE a.accessLog = :log", AccessAnomaly.class)
                .setParameter("log", log)
                .getResultList();
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
        return em.createQuery("SELECT a FROM AccessLog a WHERE a.device IS NOT NULL AND a.device.entrance IS NOT NULL AND a.device.entrance.entranceDeviceId  = :entranceId ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("entranceId", entranceId)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AccessLog> getAllRecentAccessAttemptsByEntrance(String entranceId) {
        return em.createQuery("SELECT a FROM AccessLog a "
                + "WHERE a.device IS NOT NULL AND a.device.entrance IS NOT NULL "
                + "AND a.device.entrance.entranceDeviceId = :entranceId "
                + "ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("entranceId", entranceId)
                .getResultList();
    }

    public Map<String, Integer> countAccessResultsByEntrance(LocalDateTime start, LocalDateTime end, int entranceId) {

        List<Object[]> results = em.createQuery(
                "SELECT a.result, COUNT(a) FROM AccessLog a "
                + "WHERE a.result IN ('GRANTED', 'DENIED') "
                + "AND a.device IS NOT NULL "
                + "AND a.device.entrance IS NOT NULL "
                + "AND a.device.entrance.id = :entranceId "
                + "AND a.timestamp BETWEEN :start AND :end "
                + "GROUP BY a.result", Object[].class)
                .setParameter("entranceId", entranceId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<String, Integer> resultCounts = new HashMap<>();
        resultCounts.put("GRANTED", 0);
        resultCounts.put("DENIED", 0);

        for (Object[] row : results) {
            String result = (String) row[0];
            Long count = (Long) row[1];
            if (result.equals("GRANTED") || result.equals("DENIED")) {
                resultCounts.put(result, count.intValue());
            }
        }
        System.out.println("Results count >>>>>>" + resultCounts);
        return resultCounts;
    }

    public Map<String, Integer> countAccessResultsForAllEntrances(LocalDateTime start, LocalDateTime end) {

        List<Object[]> results = em.createQuery(
                "SELECT a.result, COUNT(a) FROM AccessLog a "
                + "WHERE a.result IN ('GRANTED', 'DENIED') "
                + "AND a.timestamp BETWEEN :start AND :end "
                + "GROUP BY a.result", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<String, Integer> resultCounts = new HashMap<>();
        resultCounts.put("GRANTED", 0);
        resultCounts.put("DENIED", 0);

        for (Object[] row : results) {
            String result = (String) row[0];
            Long count = (Long) row[1];
            if (result.equals("GRANTED") || result.equals("DENIED")) {
                resultCounts.put(result, count.intValue());
            }
        }

        return resultCounts;
    }

    public Map<String, Integer> getTop5RecentEntrancesByGrantedAccess(LocalDateTime start, LocalDateTime end) {

        List<Object[]> results = em.createQuery(
                "SELECT a.device.entrance.id, a.device.entrance.entranceName, COUNT(a), MAX(a.timestamp) "
                + "FROM AccessLog a "
                + "WHERE a.result = 'GRANTED' "
                + "AND a.timestamp BETWEEN :start AND :end "
                + "AND a.device IS NOT NULL "
                + "AND a.device.entrance IS NOT NULL "
                + "GROUP BY a.device.entrance.id, a.device.entrance.entranceName "
                + "ORDER BY MAX(a.timestamp) DESC", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .setMaxResults(5)
                .getResultList();

        Map<String, Integer> entranceCounts = new HashMap<>();
        for (Object[] row : results) {
            Integer entranceId = (Integer) row[0];
            String entranceName = (String) row[1];
            Long count = (Long) row[2];

            entranceCounts.put(entranceName, count.intValue());
        }

        return entranceCounts;
    }

    //Get THE top 5 entry and exitmover last day 
    public List<EntranceAccessStatsDTO> getTop5EntranceAccessStats(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = em.createQuery(
                "SELECT a.device.entrance, a.device.devicePosition, COUNT(a), MAX(a.timestamp) "
                + "FROM AccessLog a "
                + "WHERE a.result = :granted "
                + "AND a.timestamp BETWEEN :start AND :end "
                + "GROUP BY a.device.entrance, a.device.devicePosition", Object[].class)
                .setParameter("granted", "GRANTED")
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<Integer, EntranceAccessStatsDTO> dtoMap = new HashMap<>();
        Map<Integer, LocalDateTime> lastAccessMap = new HashMap<>();

        for (Object[] row : results) {
            Entrances entrance = (Entrances) row[0];
            DevicePosition position = (DevicePosition) row[1];
            Long count = (Long) row[2];
            LocalDateTime lastTime = (LocalDateTime) row[3];

            dtoMap.computeIfAbsent(entrance.getId(),
                    id -> new EntranceAccessStatsDTO(entrance, 0, 0));

            if (position == DevicePosition.ENTRY) {
                dtoMap.get(entrance.getId()).setEntryCount(count.intValue());
            } else if (position == DevicePosition.EXIT) {
                dtoMap.get(entrance.getId()).setExitCount(count.intValue());
            }

            lastAccessMap.put(entrance.getId(), lastTime);
        }

// Sort by recent activity
        return lastAccessMap.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(entry -> dtoMap.get(entry.getKey()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<String, Map<String, Integer>> getTop5RecentEntrancesByAccessResult(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = em.createQuery(
                "SELECT a.device.entrance.id, a.result, COUNT(a), MAX(a.timestamp) "
                + "FROM AccessLog a "
                + "WHERE a.timestamp BETWEEN :start AND :end "
                + "AND a.device IS NOT NULL "
                + "AND a.device.entrance IS NOT NULL "
                + "GROUP BY a.device.entrance.id, a.result "
                + "ORDER BY MAX(a.timestamp) DESC", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        // Track last access timestamp per entrance
        Map<Integer, LocalDateTime> entranceLastAccess = new HashMap<>();
        Map<Integer, Map<String, Integer>> accessMap = new HashMap<>();

        for (Object[] row : results) {
            Integer entranceId = (Integer) row[0];
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
        List<Integer> top5EntranceIds = entranceLastAccess.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(Map.Entry::getKey)
                .distinct()
                .limit(5)
                .toList();

        // Step 3: Build final result map with entrance names
        Map<String, Map<String, Integer>> finalResult = new LinkedHashMap<>();
        for (Integer entranceId : top5EntranceIds) {
            Entrances entrance = entrancesService.findEntranceById(entranceId);
            String entranceName = entrance != null ? entrance.getEntranceName() : "Entrance Not Found";
            finalResult.put(entranceName, accessMap.get(entranceId));//Entrance Not found
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

    private boolean hasRoleOrCustomAccess(Employee employee, Entrances entrance) {

        if (employee == null || entrance == null) {
            return false;
        }

        Integer targetEntranceId = entrance.getId(); // Use primary key (ID) comparison

        // Check role-based access
        EmployeeRole role = employee.getRole();
        if (role != null && role.getAccessibleEntrances() != null) {
            boolean hasRoleAccess = role.getAccessibleEntrances()
                    .stream()
                    .anyMatch(e -> targetEntranceId.equals(e.getId()));

            if (hasRoleAccess) {
                return true;
            }
        }

        // Check custom access
        if (employee.getCustomEntrances() != null) {
            boolean hasCustomAccess = employee.getCustomEntrances()
                    .stream()
                    .anyMatch(e -> targetEntranceId.equals(e.getId()));

            if (hasCustomAccess) {
                return true;
            }
        }

        return false; // Neither role-based nor custom access

    }

    // method to check access
    public boolean hasAccess(Employee employee, Devices devices) {

        if (employee == null) {
            System.out.println("employee is null");
            return false;
        }
        if (devices == null) {
            System.out.println("device is null");
            return false;
        }
        if (devices.getEntrance() == null) {
            System.out.println("No entrance assigned to this device");
            JSF.addErrorMessage("No entrance assigned to this device");
            return false;
        }

        Entrances entrance = devices.getEntrance();
        EntranceMode mode = entrance.getEntranceMode();
        DevicePosition position = devices.getDevicePosition();

        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        boolean hasBaseAccess = hasRoleOrCustomAccess(employee, entrance);
        if (!hasBaseAccess) {
            System.out.println("Access denied: no role or custom access.");
            return false;
        }

        //==============IN FULL ACCESS MODE=========
        if (mode == EntranceMode.FULL_ACCESS) {
            // ENTRY and EXIT both require only role/custom check (already passed)
            return true;
        }

        // ========== LENIENT Mode ==========
        if (mode == EntranceMode.LENIENT) {
            if (position == DevicePosition.ENTRY) {
                // Must pass time-based check
                boolean withinRoleTime = isWithinRoleTimeRule(employee.getRole(), now, today, entrance);
                boolean withinCustomTime = isWithinCustomAccess(employee, entrance, today, now);

                if (!withinRoleTime && !withinCustomTime) {
                    System.out.println("Access denied: outside both time ranges.");
                    //update accesslog WITH flag or reason
                    return false;
                }

                if (!withinRoleTime) {
                    System.out.println("Warning: outside role time range, but custom access allowed.");
                } else if (!withinCustomTime) {
                    System.out.println("Warning: outside custom time range, but role access allowed.");
                }

                return true; //  At least one time rule passed
            }
            if (position == DevicePosition.EXIT) {
                // No time check needed
                return true;
            }
        }

        // ===== STRICT Mode Rules =====
        if (mode == EntranceMode.STRICT) {
            EmployeeEntranceState state = employeeEntranceStateService
                    .findByEmployeeAndEntrance(employee.getId(), entrance.getId());

            if (position == DevicePosition.ENTRY) {
                boolean withinRoleTime = isWithinRoleTimeRule(employee.getRole(), now, today, entrance);
                boolean withinCustomTime = isWithinCustomAccess(employee, entrance, today, now);

                //check if the person is within time
                if (!withinRoleTime && !withinCustomTime) {
                    System.out.println("Denied ENTRY: outside both role and custom time.");
                    return false;
                }

                if (state != null && state.getCurrentState() == LocationState.INSIDE) {
                    System.out.println("Denied: already INSIDE.");
                    return false;
                }
                return true; // Allowed to enter
            }

            if (position == DevicePosition.EXIT) {
                if (state == null || state.getCurrentState() != LocationState.INSIDE) {
                    System.out.println("Denied EXIT: not currently INSIDE.");
                    return false;
                }

                boolean withinRoleTime = isWithinRoleTimeRule(employee.getRole(), now, today, entrance);
                boolean withinCustomTime = isWithinCustomAccess(employee, entrance, today, now);

                if (!withinRoleTime && !withinCustomTime) {
                    System.out.println("Warning: EXIT outside allowed time, but allowed in STRICT mode.");
                    //log here
                }

                return true; // Allowed to exit
            }
            return false;
        }

        System.out.println(
                "Access denied for employee>>>>>>: " + employee + " entrance>>>>>>>: " + entrance);

        return false;
    }

    public AccessEvaluationResult evaluateAccess(Employee employee, Devices device) {
        AccessEvaluationResult result = new AccessEvaluationResult();
        result.setEmployee(employee);
        result.setDevice(device);

        LocalDateTime now = LocalDateTime.now();
        result.setTimestamp(now);

        if (device == null) {
            result.setGranted(false);
            result.setResult("DENIED");
            result.setMessage("Device not found.");
            return result;
        }

        Entrances entrance = device.getEntrance();
        if (entrance == null) {
            result.setGranted(false);
            result.setResult("DENIED");
            result.setDevice(device);
            result.setMessage("Device is not assigned to any entrance.");
            return result;
        }

        result.setEntrance(entrance);

        if (employee == null) {
            result.setGranted(false);
            result.setResult("DENIED");
            result.setMessage("Employee not found.");
            return result;
        }

        //Has role Access or custom Access
        boolean hasBaseAccess = hasRoleOrCustomAccess(employee, entrance);
        if (!hasBaseAccess) {
            result.setGranted(false);
            result.setResult("DENIED");
            result.setMessage("Employee does not have role or custom access to this entrance.");
            JSF.addErrorMessage("You don't have access to this entrance");
            return result;
        }

        // Step 2: Access mode-specific rules
        EntranceMode mode = entrance.getEntranceMode();
        DevicePosition position = device.getDevicePosition();

        LocalTime timeNow = now.toLocalTime();
        DayOfWeek day = now.getDayOfWeek();
        boolean withinRoleTime = isWithinRoleTimeRule(employee.getRole(), timeNow, day, entrance);
        boolean withinCustomTime = isWithinCustomAccess(employee, entrance, day, timeNow);

        //==============IN FULL ACCESS MODE=========
        if (mode == EntranceMode.FULL_ACCESS) {
            result.setGranted(true);
            result.setResult("GRANTED");
            result.setMessage("Full access granted.");
            return result;
        }

        // ========== LENIENT Mode ==========
        if (mode == EntranceMode.LENIENT) {
            if (position == DevicePosition.ENTRY) {
                if (!withinRoleTime && !withinCustomTime) {
                    result.setGranted(false);
                    result.setResult("DENIED");
                    result.setMessage("Entry denied: Attempted entry outside allowed time");
                    result.addAnomaly(AnomalyType.OUT_OF_TIME_RANGE_ENTRY);
                    return result;
                }

                result.setGranted(true);
                result.setResult("GRANTED");
                result.setMessage("Entry allowed.");
                return result;
            }

            if (position == DevicePosition.EXIT) {
                if (!withinRoleTime && !withinCustomTime) {
                    result.addAnomaly(AnomalyType.OUT_OF_TIME_RANGE_EXIT);
                    result.setGranted(true);
                    result.setResult("GRANTED");
                    result.setMessage("Exit Allowed: Outside Time Range.");
                    return result;
                }
                result.setGranted(true);
                result.setResult("GRANTED");
                result.setMessage("Exit allowed in lenient mode.");
                return result;
            }
        }

        // ========== STRICT Mode ==========
        if (mode == EntranceMode.STRICT) {
            EmployeeEntranceState state = employeeEntranceStateService
                    .findByEmployeeAndEntrance(employee.getId(), entrance.getId());

            if (position == DevicePosition.ENTRY) {
                if (!withinRoleTime && !withinCustomTime) {
                    result.setGranted(false);
                    result.setResult("DENIED");
                    result.setMessage("Entry denied:  Attempted entry outside allowed time");
                    result.addAnomaly(AnomalyType.OUT_OF_TIME_RANGE_ENTRY);
                    return result;
                }

                if (state != null && state.getCurrentState() == LocationState.INSIDE) {
                    result.setGranted(false);
                    result.setResult("DENIED");
                    result.setMessage("Entry denied:  Didn't badge out.");
                    result.addAnomaly(AnomalyType.STRICT_MODE_VIOLATION);
                    return result;
                }

                result.setGranted(true);
                result.setResult("GRANTED");
                result.setMessage("Strict entry allowed.");
                return result;
            }

            if (position == DevicePosition.EXIT) {

                if (state == null || state.getCurrentState() == LocationState.OUTSIDE) {
                    result.setGranted(false);
                    result.setResult("DENIED");
                    result.setMessage("Exit denied: Didn't badge in.");
                    result.addAnomaly(AnomalyType.STRICT_MODE_VIOLATION);
                    return result;
                } else if (!withinRoleTime && !withinCustomTime) {
                    result.setGranted(true);
                    result.setResult("GRANTED");
                    result.setMessage("Exit allowed, but occurred outside allowed time.");
                    result.addAnomaly(AnomalyType.OUT_OF_TIME_RANGE_EXIT);
                    return result;
                }

                result.setGranted(true);
                result.setResult("GRANTED");
                result.setMessage("Strict exit allowed.");
                return result;
            }

        }
        result.setGranted(false);
        result.setResult("DENIED");
        result.setMessage("Unhandled entrance mode or device position.");
        return result;
    }

    public int countFilteredAccessLogs(String employeeName, String ghanaCardNumber, String entranceId,
            String result, LocalDateTime startTime, LocalDateTime endTime) {

        StringBuilder queryStr = new StringBuilder("SELECT COUNT(a) FROM AccessLog a WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (ghanaCardNumber != null && !ghanaCardNumber.isEmpty()) {
            queryStr.append(" AND a.employee IS NOT NULL AND a.employee.ghanaCardNumber LIKE :ghanaCardNumber");
            params.put("ghanaCardNumber", "%" + ghanaCardNumber + "%");
        }

        if (entranceId != null && !entranceId.isEmpty()) {
            queryStr.append(" AND a.device IS NOT NULL AND a.device.entrance IS NOT NULL AND a.device.entrance.entranceDeviceId = :entranceId");
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

        TypedQuery<Long> query = em.createQuery(queryStr.toString(), Long.class
        );
        params.forEach(query::setParameter);

        return query.getSingleResult().intValue();
    }

    public List<AccessLog> filterAccessLog(List<LocalDateTime> dateRange, int entranceId, String ghanacardnumber, String employeeName, Integer roleId, String result) {

        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM AccessLog a WHERE 1=1 ");

        Map<String, Object> params = new HashMap<>();

        if (dateRange != null) {

            queryBuilder.append("AND a.timestamp BETWEEN :start AND :end ");

            params.put("start", dateRange.get(0));

            params.put("end", dateRange.get(1));

        }

        if (entranceId != 0) {

            queryBuilder.append("AND a.device IS NOT NULL AND a.device.entrance IS NOT NULL AND a.device.entrance.id = :entranceId ");

            params.put("entranceId", entranceId);

        }

        if (ghanacardnumber != null && !ghanacardnumber.isBlank()) {

            queryBuilder.append("AND a.employee IS NOT NULL AND UPPER(a.employee.ghanaCardNumber) LIKE :ghanacardnumber ");

            params.put("ghanacardnumber", "%" + ghanacardnumber.toUpperCase() + "%");

        }

        if (employeeName != null && !employeeName.isBlank()) {
            queryBuilder.append("AND a.employee IS NOT NULL AND (CONCAT(LOWER(a.employee.firstname), ' ', LOWER(a.employee.lastname)) LIKE :employeeName ");
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

        TypedQuery<AccessLog> query = em.createQuery(queryBuilder.toString(), AccessLog.class
        );

        params.forEach(query::setParameter);

        return query.getResultList();

    }

    public long countAllLogs() {
        return em.createQuery("SELECT COUNT(a) FROM AccessLog a", Long.class).getSingleResult();
    }

    public long getTotalAnomalyCount() {
        return (Long) em.createQuery("SELECT COUNT(a) FROM AccessAnomaly a").getSingleResult();
    }

    public List<AccessAnomaly> getMostRecentAnomalies(int count) {
        return em.createQuery(
                "SELECT a FROM AccessAnomaly a ORDER BY a.timestamp DESC",
                AccessAnomaly.class)
                .setMaxResults(count)
                .getResultList();
    }

    public List<AccessLog> findLogsBetweenDates(LocalDateTime start, LocalDateTime end, Integer entranceId) {
        String jpql = "SELECT a FROM AccessLog a WHERE a.timestamp BETWEEN :start AND :end";
        if (entranceId != null && entranceId != 0) {
            jpql += " AND a.device.entrance.id = :entranceId";
        }

        TypedQuery<AccessLog> query = em.createQuery(jpql, AccessLog.class)
                .setParameter("start", start)
                .setParameter("end", end);

        if (entranceId != null && entranceId != 0) {
            query.setParameter("entranceId", entranceId);
        }

        return query.getResultList();
    }

    public boolean existsFor(AccessLog accessLog, AnomalyType anomalyType) {
        if (accessLog == null || anomalyType == null) {
            return false;
        }

        List<AccessAnomaly> anomalies = findAnomalyByAccessLog(accessLog);

        return anomalies.stream()
                .anyMatch(a -> a.getAnomalyType() == anomalyType);
    }

    /**
     * Count denied access attempts in a time window for specific
     * employee+device
     */
    public Long countDeniedAttemptsInTimeWindow(int employeeId, int deviceId,
            LocalDateTime start, LocalDateTime end) {
        return em.createQuery(
                "SELECT COUNT(a) FROM AccessLog a "
                + "WHERE a.employee.id = :employeeId "
                + "AND a.device.id = :deviceId "
                + "AND a.result = :result "
                + "AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("employeeId", employeeId)
                .setParameter("deviceId", deviceId)
                .setParameter("result", "DENIED")
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
    }

    /**
     * Check if we've already logged a frequent denial anomaly recently Prevents
     * spam logging of the same issue
     */
    public boolean hasRecentFrequentDenialAnomaly(int employeeId, int deviceId, LocalDateTime since) {
        Long count = em.createQuery(
                "SELECT COUNT(aa) FROM AccessAnomaly aa "
                + "WHERE aa.employee.id = :employeeId "
                + "AND aa.device.id = :deviceId "
                + "AND aa.anomalyType = :anomalyType "
                + "AND aa.timestamp >= :since", Long.class)
                .setParameter("employeeId", employeeId)
                .setParameter("deviceId", deviceId)
                .setParameter("anomalyType", AnomalyType.FREQUENT_DENIED_ACCESS)
                .setParameter("since", since)
                .getSingleResult();

        return count > 0;
    }
}
