/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.Bean.UserSession;
import com.margins.STIM.entity.ActivityLog;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
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
public class ActivityLogService {

    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private UserSession userSession;

    public void logActivity(ActivityLog log) {
        em.persist(log);
    }

    // For dashboard later
    public List<ActivityLog> getRecentActivities(String action, int limit) {
        return em.createQuery("SELECT a FROM ActivityLog a WHERE a.action = :action ORDER BY a.timestamp DESC", ActivityLog.class)
                .setParameter("action", action)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<ActivityLog> getRecentActivitiesByUser(String action, String userId, int limit) {
        return em.createQuery("SELECT a FROM ActivityLog a WHERE a.action = :action AND a.userId = :userId ORDER BY a.timestamp DESC", ActivityLog.class)
                .setParameter("action", action)
                .setParameter("userId", userId)
                .setMaxResults(limit)
                .getResultList();
    }

    public int countByActionAndResultInPeriod(List<String> actions, String result, LocalDateTime start, LocalDateTime end) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM ActivityLog a WHERE a.action IN :actions AND a.result = :result "
                + "AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("actions", actions)
                .setParameter("result", result)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }
    
    public Map<String, Integer> countBiometricLoginsByResultsInPeriod(LocalDateTime start, LocalDateTime end) {
        System.out.println("Searching between " + start + " and " + end);

        List<Object[]> results = em.createQuery(
                "SELECT a.result, COUNT(a) FROM ActivityLog a WHERE a.action = 'login' "
                + "AND (a.details LIKE '%SingleFinger%' OR a.details LIKE '%MultiFinger%' OR a.details LIKE '%Face login%') "
                + "AND a.timestamp BETWEEN :start AND :end "
                + "GROUP BY a.result", Object[].class)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();

        Map<String, Integer> resultCounts = new HashMap<>();
        resultCounts.put("Succesful", 0);
        resultCounts.put("Failed", 0);

        for (Object[] row : results) {
            String result = (String) row[0];
            Long count = (Long) row[1];
            if (result.contains("Success")) {
                resultCounts.put("Success", count.intValue());
            } else if (result.contains("Failed")) {
                resultCounts.put("Failed", count.intValue());
            }
        }

        System.out.println("Query returned: Success=" + resultCounts.get("Success") + ", Failed=" + resultCounts.get("Failed"));
        return resultCounts;
    }

    public int countByActionAndResultAndUser(List<String> actions, String result, String userId, LocalDateTime start, LocalDateTime end) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM ActivityLog a WHERE a.action IN :actions AND a.result = :result "
                + "AND a.userId = :userId AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("actions", actions)
                .setParameter("result", result)
                .setParameter("userId", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }
    
    
    public int countEmployeesOnboardedByLoggedInUserInDay(LocalDate day) {
        // Get logged-in user's username from session
        String userId = userSession.getUsername();

        if (userId == null) {
            throw new IllegalStateException("No logged-in user found");
        }

        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.atTime(23, 59, 59);

        //Rectify this to use auditLog to check employees onboarded this day, week or month
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM ActivityLog a "
                + "WHERE a.action = 'create_employee' "
                + "AND a.userId = :userId "
                + "AND a.timestamp BETWEEN :start AND :end", Long.class)
                .setParameter("userId", userId)
                .setParameter("start", startOfDay)
                .setParameter("end", endOfDay)
                .getSingleResult();

        return count.intValue();
    }
}
