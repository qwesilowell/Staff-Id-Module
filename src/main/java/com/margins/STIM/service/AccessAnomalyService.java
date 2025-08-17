/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.AnomalySeverity;
import com.margins.STIM.entity.enums.AnomalyStatus;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class AccessAnomalyService {

    @PersistenceContext
    private EntityManager em;

    public List<AccessAnomaly> findAllPending() {
        return em.createQuery("SELECT a FROM AccessAnomaly a WHERE a.status = :status ORDER BY a.timestamp DESC", AccessAnomaly.class)
                .setParameter("status", AnomalyStatus.PENDING)
                .getResultList();
    }

    public List<AccessAnomaly> findAllUnattended() {
        return em.createQuery("SELECT a FROM AccessAnomaly a WHERE a.status = :status ORDER BY a.timestamp DESC", AccessAnomaly.class)
                .setParameter("status", AnomalyStatus.UNATTENDED)
                .getResultList();
    }

    public List<AccessAnomaly> findAllResolved() {
        return em.createQuery("SELECT a FROM AccessAnomaly a WHERE a.status = :status ORDER BY a.timestamp DESC", AccessAnomaly.class)
                .setParameter("status", AnomalyStatus.RESOLVED)
                .getResultList();
    }

    public long countByStatus(AnomalyStatus status) {
        return em.createQuery("SELECT COUNT(a) FROM AccessAnomaly a WHERE a.anomalyStatus = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    public void markAsResolved(int anomalyId, Users resolvedBy) {
        AccessAnomaly anomaly = em.find(AccessAnomaly.class, anomalyId);
        if (anomaly != null && anomaly.getAnomalyStatus() != AnomalyStatus.RESOLVED) {
            anomaly.setAnomalyStatus(AnomalyStatus.RESOLVED);
            anomaly.setHandledBy(resolvedBy);
            anomaly.setResolvedAt(LocalDateTime.now());
            em.merge(anomaly);
        }
    }

    public void resolveBatch(List<Integer> anomalyIds, Users resolvedBy) {
        for (int id : anomalyIds) {
            markAsResolved(id, resolvedBy);
        }
    }

    public List<AccessAnomaly> findBySeverity(AnomalySeverity severity) {
        return em.createQuery("SELECT a FROM AccessAnomaly a WHERE a.anomalySeverity = :severity ORDER BY a.timestamp DESC", AccessAnomaly.class)
                .setParameter("severity", severity)
                .getResultList();
    }

    public List<AccessAnomaly> findBySeverityAndStatus(AnomalySeverity severity, AnomalyStatus status) {
        return em.createQuery("SELECT a FROM AccessAnomaly a WHERE a.anomalySeverity = :severity AND a.anomalyStatus = :status ORDER BY a.timestamp DESC", AccessAnomaly.class)
                .setParameter("severity", severity)
                .setParameter("status", status)
                .getResultList();
    }

    public long countBySeverity(AnomalySeverity severity) {
        return em.createQuery("SELECT COUNT(a) FROM AccessAnomaly a WHERE a.anomalySeverity = :severity", Long.class)
                .setParameter("severity", severity)
                .getSingleResult();
    }

    public long countBySeverityAndStatus(AnomalySeverity severity, AnomalyStatus status) {
        return em.createQuery("SELECT COUNT(a) FROM AccessAnomaly a WHERE a.anomalySeverity = :severity AND a.anomalyStatus = :status", Long.class)
                .setParameter("severity", severity)
                .setParameter("status", status)
                .getSingleResult();
    }

    public List<AccessAnomaly> findWithFilters(AnomalySeverity severity, AnomalyStatus status,
            String employeeName, Integer deviceId,
            Integer entranceId, LocalDate dateFilter) {
        StringBuilder jpql = new StringBuilder("SELECT a FROM AccessAnomaly a WHERE 1=1");

        if (severity != null) {
            jpql.append(" AND a.anomalySeverity = :severity");
        }
        if (status != null) {
            jpql.append(" AND a.anomalyStatus = :status");
        }
        if (employeeName != null && !employeeName.trim().isEmpty()) {
            jpql.append(" AND (LOWER(CONCAT(a.employee.firstname, ' ', a.employee.lastname)) LIKE :employeeName");
            jpql.append(" OR LOWER(a.employee.firstname) LIKE :employeeName");
            jpql.append(" OR LOWER(a.employee.lastname) LIKE :employeeName)");
        }
        if (deviceId != null) {
            jpql.append(" AND a.device.id = :deviceId");
        }
        if (entranceId != null) {
            jpql.append(" AND a.entrance.id = :entranceId");
        }
        if (dateFilter != null) {
            jpql.append(" AND DATE(a.timestamp) = :dateFilter");
        }

        jpql.append(" ORDER BY a.timestamp DESC");

        TypedQuery<AccessAnomaly> query = em.createQuery(jpql.toString(), AccessAnomaly.class);

        if (severity != null) {
            query.setParameter("severity", severity);
        }
        if (status != null) {
            query.setParameter("status", status);
        }
        if (employeeName != null && !employeeName.trim().isEmpty()) {
            query.setParameter("employeeName", "%" + employeeName.toLowerCase() + "%");
        }
        if (deviceId != null) {
            query.setParameter("deviceId", deviceId);
        }
        if (entranceId != null) {
            query.setParameter("entranceId", entranceId);
        }
        if (dateFilter != null) {
            query.setParameter("dateFilter", dateFilter);
        }

        return query.getResultList();
    }

    public void markAsPending(int anomalyId, Users handledBy) {
        AccessAnomaly anomaly = em.find(AccessAnomaly.class, anomalyId);
        if (anomaly != null && anomaly.getAnomalyStatus() == AnomalyStatus.UNATTENDED) {
            anomaly.setAnomalyStatus(AnomalyStatus.PENDING);
            anomaly.setHandledBy(handledBy);
            em.merge(anomaly);
        }
    }

    public void assignHandler(int anomalyId, Users handler) {
        AccessAnomaly anomaly = em.find(AccessAnomaly.class, anomalyId);
        if (anomaly != null) {
            anomaly.setHandledBy(handler);
            em.merge(anomaly);
        }
    }

}
