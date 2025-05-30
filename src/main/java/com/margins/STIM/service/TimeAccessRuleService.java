/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.CustomTimeAccess;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;
import com.margins.STIM.entity.TimeAccessRule;
import com.margins.STIM.util.DateFormatter;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    
    
    
    public void saveOrUpdateRoleTimeAccess(EmployeeRole role, Entrances entrance,
            Map<String, LocalTime> startTimes,
            Map<String, LocalTime> endTimes,
            List<String> selectedDays) {
        for (String dayStr : selectedDays) {
            DayOfWeek day = DayOfWeek.valueOf(dayStr.toUpperCase());

            // Try to find existing rule
            RoleTimeAccess existing = findByRoleEntranceAndDay(role, entrance, day);

            
            LocalTime startLocalTime = startTimes.get(dayStr);
            LocalTime endLocalTime = endTimes.get(dayStr);
            
            Date startTime = DateFormatter.toDate(startLocalTime);
            Date endTime = DateFormatter.toDate(endLocalTime);


            if (existing != null) {
                existing.setStartTime(startTime);
                existing.setEndTime(endTime);
                em.merge(existing); // update
            } else {
                RoleTimeAccess newRule = new RoleTimeAccess();
                newRule.setEmployeeRole(role);
                newRule.setEntrances(entrance);
                newRule.setDayOfWeek(day);
                newRule.setStartTime(startTime);
                newRule.setEndTime(endTime);
                em.persist(newRule); // insert
            }
        }
    }
    
    private RoleTimeAccess findByRoleEntranceAndDay(EmployeeRole role, Entrances entrance, DayOfWeek day) {
        try {
            return em.createQuery("SELECT r FROM RoleTimeAccess r WHERE r.employeeRole = :role AND r.entrances = :entrance AND r.dayOfWeek = :day", RoleTimeAccess.class)
                    .setParameter("role", role)
                    .setParameter("entrance", entrance)
                    .setParameter("day", day)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    
    
    public void saveOrUpdateCustomTimeAccess(Employee employee, Entrances entrance,
            Map<String, LocalTime> startTimes,
            Map<String, LocalTime> endTimes,
            List<String> selectedDays) {
        for (String dayStr : selectedDays) {
            DayOfWeek day = DayOfWeek.valueOf(dayStr.toUpperCase());

            // Try to find existing rule
            CustomTimeAccess existing = findByEmployeeEntranceAndDay(employee, entrance, day);

            LocalTime startLocalTime = startTimes.get(dayStr);
            LocalTime endLocalTime = endTimes.get(dayStr);

            Date startTime = DateFormatter.toDate(startLocalTime);
            Date endTime = DateFormatter.toDate(endLocalTime);

            if (existing != null) {
                existing.setStartTime(startTime);
                existing.setEndTime(endTime);
                em.merge(existing); // update
            } else {
                CustomTimeAccess newRule = new CustomTimeAccess();
                newRule.setEmployee(employee);
                newRule.setEntrances(entrance);
                newRule.setDayOfWeek(day);
                newRule.setStartTime(startTime);
                newRule.setEndTime(endTime);
                em.persist(newRule); // insert
            }
        }
    }
    
    private CustomTimeAccess findByEmployeeEntranceAndDay(Employee employee, Entrances entrance, DayOfWeek day) {
        try {
            return em.createQuery("SELECT r FROM CustomTimeAccess r WHERE r.employee = :employee AND r.entrances = :entrance AND r.dayOfWeek = :day", CustomTimeAccess.class)
                    .setParameter("employee", employee)
                    .setParameter("entrance", entrance)
                    .setParameter("day", day)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
