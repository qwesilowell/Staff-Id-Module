/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AuditLog;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
public class AuditLogService {

    @PersistenceContext
    private EntityManager em;

//    public void logActivity(AuditLog log) {
//        em.persist(log);
//    }
    public AuditLog findAuditById(Long id) {
        return em.find(AuditLog.class, id);
    }

    public List<AuditLog> getAllActivities() {
        return em.createQuery("SELECT a FROM AuditLog a ORDER BY a.createdOn DESC", AuditLog.class)
                .getResultList();
    }

    public void logActivity(AuditActionType action, String entityName, ActionResult result, String details, Users performedBy) {
        System.out.println("=== AUDIT LOG DEBUG ===");
        System.out.println("Action: " + action);
        System.out.println("Entity Name: " + entityName);
        System.out.println("Result: " + result);
        System.out.println("Details: " + details);
        System.out.println("Performed By: " + (performedBy != null ? performedBy.getUsername() : "NULL"));

        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityName(entityName);
            auditLog.setResult(result);
            auditLog.setDetails(details);
            auditLog.setPerformedBy(performedBy);
            auditLog.setCreatedOn(LocalDateTime.now());

            System.out.println("About to persist audit log...");
            em.persist(auditLog);
            em.flush(); // Force immediate database write
            System.out.println("Audit log persisted successfully");
            System.out.println("=== END AUDIT LOG DEBUG ===");

        } catch (Exception e) {
            System.out.println("ERROR persisting audit log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int countByActionAndResultInPeriod(AuditActionType actions, ActionResult result, LocalDateTime start, LocalDateTime end) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM AuditLog a WHERE a.action = :actions AND a.result = :result "
                + "AND a.createdOn BETWEEN :start AND :end", Long.class)
                .setParameter("actions", actions)
                .setParameter("result", result)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }

    public int countByActionAndResultAndUser(AuditActionType actions, ActionResult result, Users userId, LocalDateTime start, LocalDateTime end) {
        Long count = em.createQuery(
                "SELECT COUNT(a) FROM AuditLog a WHERE a.action = :actions AND a.result = :result "
                + "AND a.performedBy = :userId AND a.createdOn BETWEEN :start AND :end", Long.class)
                .setParameter("actions", actions)
                .setParameter("result", result)
                .setParameter("userId", userId)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();
        return count.intValue();
    }

    public List<AuditLog> filterAuditLogs(List<LocalDateTime> dateRange,
            AuditActionType action,
            ActionResult result,
            Users performedByName,
            String detailsText) {

        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM AuditLog a WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (dateRange != null && dateRange.size() == 2) {
            queryBuilder.append("AND a.createdOn BETWEEN :start AND :end ");
            params.put("start", dateRange.get(0));
            params.put("end", dateRange.get(1));
        }

        if (action != null) {
            queryBuilder.append("AND a.action = :action ");
            params.put("action", action);
        }

        if (result != null) {
            queryBuilder.append("AND a.result = :result ");
            params.put("result", result);
        }

        if (performedByName != null) {
            queryBuilder.append("AND a.performedBy = :performedByName ");
            params.put("performedByName", performedByName);
        }

        if (detailsText != null && !detailsText.isBlank()) {
            queryBuilder.append("AND LOWER(a.details) LIKE :details ");
            params.put("details", "%" + detailsText.toLowerCase() + "%");
        }

        queryBuilder.append("ORDER BY a.createdOn DESC");

        TypedQuery<AuditLog> query = em.createQuery(queryBuilder.toString(), AuditLog.class);
        params.forEach(query::setParameter);

        return query.getResultList();
    }

    public List<AuditLog> getRecentActivities(int limit) {
        return em.createQuery(
                "SELECT a FROM AuditLog a ORDER BY a.createdOn DESC",
                AuditLog.class
        )
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> getRecentActivitiesByAction(AuditActionType action, int limit) {
        return em.createQuery("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.createdOn DESC", AuditLog.class)
                .setParameter("action", action)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> getRecentActivitiesByUserAndAction(AuditActionType action, Users user, int limit) {
        return em.createQuery("SELECT a FROM AuditLog a WHERE a.action = :action AND a.performedBy = :user ORDER BY a.createdOn DESC", AuditLog.class)
                .setParameter("action", action)
                .setParameter("user", user)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> getRecentActivitiesByUser(Users user, int limit) {
        return em.createQuery("SELECT a FROM AuditLog a WHERE a.performedBy = :user ORDER BY a.createdOn DESC", AuditLog.class)
                .setParameter("user", user)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<AuditLog> filterAuditLogs(List<LocalDateTime> dateRange, AuditActionType action, ActionResult result, Users performedByName, String detailsText, int first, int pageSize, String sortField, SortOrder sortOrder) {

        StringBuilder queryBuilder = new StringBuilder("SELECT a FROM AuditLog a WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (dateRange != null && dateRange.size() == 2) {
            queryBuilder.append("AND a.createdOn BETWEEN :start AND :end ");
            params.put("start", dateRange.get(0));
            params.put("end", dateRange.get(1));
        }

        if (action != null) {
            queryBuilder.append("AND a.action = :action ");
            params.put("action", action);
        }

        if (result != null) {
            queryBuilder.append("AND a.result = :result ");
            params.put("result", result);
        }

        if (performedByName != null) {
            queryBuilder.append("AND a.performedBy = :performedByName ");
            params.put("performedByName", performedByName);
        }

        if (detailsText != null && !detailsText.isBlank()) {
            queryBuilder.append("AND LOWER(a.details) LIKE :details ");
            params.put("details", "%" + detailsText.toLowerCase() + "%");
        }

      

        if (sortField != null && !sortField.isEmpty()) {
            String orderBy = sortOrder == SortOrder.ASCENDING ? "ASC" : "DESC";
            queryBuilder.append("ORDER BY a.").append(sortField).append(" ").append(orderBy);
        } else {
             queryBuilder.append("ORDER BY a.createdOn DESC");
        }
        
        TypedQuery<AuditLog> query = em.createQuery(queryBuilder.toString(), AuditLog.class);
        params.forEach(query::setParameter);
        
        //Set pagination
        query.setFirstResult(first);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }
    
    
        public int countFilterAuditLogs(List<LocalDateTime> dateRange,
            AuditActionType action,
            ActionResult result,
            Users performedByName,
            String detailsText) {

        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT (a) FROM AuditLog a WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (dateRange != null && dateRange.size() == 2) {
            queryBuilder.append("AND a.createdOn BETWEEN :start AND :end ");
            params.put("start", dateRange.get(0));
            params.put("end", dateRange.get(1));
        }

        if (action != null) {
            queryBuilder.append("AND a.action = :action ");
            params.put("action", action);
        }

        if (result != null) {
            queryBuilder.append("AND a.result = :result ");
            params.put("result", result);
        }

        if (performedByName != null) {
            queryBuilder.append("AND a.performedBy = :performedByName ");
            params.put("performedByName", performedByName);
        }

        if (detailsText != null && !detailsText.isBlank()) {
            queryBuilder.append("AND LOWER(a.details) LIKE :details ");
            params.put("details", "%" + detailsText.toLowerCase() + "%");
        }

        TypedQuery<Long> query = em.createQuery(queryBuilder.toString(), Long.class);
        params.forEach(query::setParameter);

        return query.getSingleResult().intValue();
    }
}
