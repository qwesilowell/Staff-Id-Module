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
import com.margins.STIM.util.DateFormatter;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Stateless
public class TimeAccessRuleService {

    @PersistenceContext
    private EntityManager em;
    
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
            return em.createQuery("SELECT r FROM RoleTimeAccess r WHERE r.employeeRole = :role AND r.entrances = :entrance AND r.dayOfWeek = :day"
                    + " AND r.deleted = false", RoleTimeAccess.class)
                    .setParameter("role", role)
                    .setParameter("entrance", entrance)
                    .setParameter("day", day)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<RoleTimeAccess> findByRoleAndEntrance(EmployeeRole role, Entrances entrance) {
        return em.createQuery("SELECT r FROM RoleTimeAccess r WHERE r.employeeRole = :role AND r.entrances = :entrance"
                + " AND r.deleted = false", RoleTimeAccess.class)
                .setParameter("role", role)
                .setParameter("entrance", entrance)
                .getResultList();
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
            return em.createQuery("SELECT r FROM CustomTimeAccess r WHERE r.deleted = false AND "
                    + "r.employee = :employee AND "
                    + "r.entrances = :entrance AND r.dayOfWeek = :day", CustomTimeAccess.class)
                    .setParameter("employee", employee)
                    .setParameter("entrance", entrance)
                    .setParameter("day", day)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
    
    public List<CustomTimeAccess> findByEmployeeAndEntrance(Employee emp, Entrances entrance) {
        return em.createQuery("SELECT r FROM CustomTimeAccess r WHERE r.deleted = false AND "
                + "r.employee = :emp AND "
                + "r.entrances = :entrance", CustomTimeAccess.class)
                .setParameter("emp", emp)
                .setParameter("entrance", entrance)
                .getResultList();
    }
    

    public void deleteCustomTimeAccess(Employee employee, Entrances entrance, String dayStr) {
        DayOfWeek day = DayOfWeek.valueOf(dayStr.toUpperCase());

        CustomTimeAccess existing = findByEmployeeEntranceAndDay(employee, entrance, day);
        if (existing != null) {
            existing.setDeleted(true);
            em.merge(existing); // Soft delete by marking it
        }
    }

    public long countCustomTimeAccessRules() {
        return em.createQuery("SELECT COUNT(c) FROM CustomTimeAccess c", Long.class).getSingleResult();
    }

    public long countRoleTimeAccessRules() {
        return em.createQuery("SELECT COUNT(r) FROM RoleTimeAccess r", Long.class).getSingleResult();
    }
}
