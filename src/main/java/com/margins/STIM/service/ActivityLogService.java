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
}
