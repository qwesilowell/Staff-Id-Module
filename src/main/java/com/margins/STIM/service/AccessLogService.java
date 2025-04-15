/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessLog;
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
public class AccessLogService {

    @PersistenceContext
    private EntityManager em;

    public void logAccess(AccessLog log) {
        em.persist(log);
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
}
