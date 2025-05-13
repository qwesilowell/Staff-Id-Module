/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
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

    public List<AccessLog> getRecentAccessAttemptsByUser(String employeeId, int limit) {
        return em.createQuery("SELECT a FROM AccessLog a WHERE a.employeeId = :employeeId ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("employeeId", employeeId)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AccessLog> getRecentAccessAttemptsByEntrance(String entranceId, int limit) {
        return em.createQuery("SELECT a FROM AccessLog a WHERE a.entranceId = :entranceId ORDER BY a.timestamp DESC", AccessLog.class)
                .setParameter("entranceId", entranceId)
                .setMaxResults(limit)
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
    
    // New method to check access
    public boolean hasAccess(String ghanaCardNumber, String entranceDeviceId) {
        Employee employee = em.find(Employee.class, ghanaCardNumber);
        if (employee == null) {
            System.out.println("Employee not found: " + ghanaCardNumber);
            return false;
        }

        // Check role-based entrances
        EmployeeRole role = employee.getRole();
        if (role != null && role.getAccessibleEntrances()!= null) {
            for (Entrances entrance : role.getAccessibleEntrances()) {
                if (entrance.getEntrance_Device_ID().equals(entranceDeviceId)) {
                    System.out.println("Access granted via role for employee: " + ghanaCardNumber + ", entrance: " + entranceDeviceId);
                    return true;
                }
            }
        }

        // Check custom entrances
        if (employee.getCustomEntrances() != null) {
            for (Entrances entrance : employee.getCustomEntrances()) {
                if (entrance.getEntrance_Device_ID().equals(entranceDeviceId)) {
                    System.out.println("Access granted via custom entrance for employee: " + ghanaCardNumber + ", entrance: " + entranceDeviceId);
                    return true;
                }
            }
        }

        System.out.println("Access denied for employee: " + ghanaCardNumber + ", entrance: " + entranceDeviceId);
        return false;
    }

}
