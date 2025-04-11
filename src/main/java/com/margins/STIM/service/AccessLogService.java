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
    public List<AccessLog> getRecentAccesses(int limit) {
        return em.createQuery("SELECT a FROM AccessLog a ORDER BY a.timestamp DESC", AccessLog.class)
                .setMaxResults(limit)
                .getResultList();
    }
}
