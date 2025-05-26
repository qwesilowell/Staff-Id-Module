/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.TimeAccessRule;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Stateless
public class TimeAccessRuleService {

    @PersistenceContext
    private EntityManager em;

    public void saveTimeRule(TimeAccessRule rule) {
        if ((rule.getRole() != null && rule.getEmployee() != null)
                || (rule.getRole() == null && rule.getEmployee() == null)) {
            throw new IllegalArgumentException("Rule must have either role or employee, not both or neither");
        }
        if (rule.getEntrance() == null) {
            throw new IllegalArgumentException("Rule must have an entrance");
        }
        if (rule.getId() == null) {
            em.persist(rule);
        } else {
            em.merge(rule);
        }
    }

    public TimeAccessRule getTimeRuleById(Long ruleId) {
        return em.find(TimeAccessRule.class, ruleId);
    }

    public List<TimeAccessRule> getTimeRulesByRole(int roleId) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.role.id = :roleId AND r.employee IS NULL",
                TimeAccessRule.class
        );
        query.setParameter("roleId", roleId);
        return query.getResultList();
    }

    public List<TimeAccessRule> getTimeRulesByEntrance(String entranceId) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.entrance.entranceDeviceId = :entranceId AND r.employee IS NULL",
                TimeAccessRule.class
        );
        query.setParameter("entranceId", entranceId);
        return query.getResultList();
    }

    public List<TimeAccessRule> getTimeRulesByEmployee(String ghanaCardNumber) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.employee.ghanaCardNumber = :ghanaCardNumber AND r.role IS NULL",
                TimeAccessRule.class
        );
        query.setParameter("ghanaCardNumber", ghanaCardNumber);
        return query.getResultList();
    }

    public TimeAccessRule getRuleByRoleAndEntrance(int roleId, String entranceId) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.role.id = :roleId "
                + "AND r.entrance.entranceDeviceId = :entranceDeviceId "
                + "AND r.employee IS NULL",
                TimeAccessRule.class
        );
        query.setParameter("roleId", roleId);
        query.setParameter("entranceDeviceId", entranceId);
        return query.getResultStream().findFirst().orElse(null);
    }

    public TimeAccessRule getRuleByEmployeeAndEntrance(String ghanaCardNumber, String entranceId) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.employee.ghanaCardNumber = :ghanaCard "
                + "AND r.entrance.entranceDeviceId= :entranceDeviceId",
                TimeAccessRule.class
        );
        query.setParameter("ghanaCard", ghanaCardNumber);
        query.setParameter("entranceDeviceId", entranceId);
        return query.getResultStream().findFirst().orElse(null);
    }

    public void deleteTimeRule(Long ruleId) {
        TimeAccessRule rule = em.find(TimeAccessRule.class, ruleId);
        if (rule != null) {
            em.remove(rule);
        }
    }

    public void deleteTimeRulesByRoleAndEntrance(int roleId, String entranceId) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.role.id = :roleId AND r.entrance.entranceDeviceId = :entranceId AND r.employee IS NULL",
                TimeAccessRule.class
        );
        query.setParameter("roleId", roleId);
        query.setParameter("entranceId", entranceId);
        List<TimeAccessRule> rules = query.getResultList();
        for (TimeAccessRule rule : rules) {
            em.remove(rule);
        }
    }

    public void deleteTimeRulesByEmployeeAndEntrance(String ghanaCardNumber, String entranceId) {
        TypedQuery<TimeAccessRule> query = em.createQuery(
                "SELECT r FROM TimeAccessRule r WHERE r.employee.ghanaCardNumber = :ghanaCardNumber AND r.entrance.entranceDeviceId= :entranceId AND r.role IS NULL",
                TimeAccessRule.class
        );
        query.setParameter("ghanaCardNumber", ghanaCardNumber);
        query.setParameter("entranceId", entranceId);
        List<TimeAccessRule> rules = query.getResultList();
        for (TimeAccessRule rule : rules) {
            em.remove(rule);
        }
    }
}
