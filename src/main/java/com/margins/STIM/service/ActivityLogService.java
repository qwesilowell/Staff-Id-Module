/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.ActivityLog;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class ActivityLogService {

    @PersistenceContext
    private EntityManager em;

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
}
