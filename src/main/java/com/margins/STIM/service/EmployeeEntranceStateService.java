/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.EmployeeEntranceStateLog;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.entity.enums.LocationState;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
@Transactional
@Stateless
public class EmployeeEntranceStateService {

    @PersistenceContext
    private EntityManager em;

    private Employee_Service employeeService;

    private EntrancesService entranceService;

    // Find all entrance states
    public List<EmployeeEntranceState> findAllEmployeeEntranceStates() {
        return em.createQuery("SELECT es FROM EmployeeEntranceState es WHERE es.deleted = false", EmployeeEntranceState.class)
                .getResultList()
                .stream()
                .sorted(Comparator.comparing(
                        (EmployeeEntranceState es) -> Optional.ofNullable(es.getLastModifiedDate()).orElse(es.getCreatedDate()),
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());
    }
    
    public List<EmployeeEntranceState> findRecentEmployeeEntranceStates(int limit) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es "
                + "WHERE es.deleted = false "
                + "ORDER BY COALESCE(es.lastModifiedDate, es.createdDate) DESC",
                EmployeeEntranceState.class
        )
                .setMaxResults(limit)
                .getResultList();
    }


    // Find entrance state by ID
    public EmployeeEntranceState findEmployeeEntranceStateById(Long id) {
        return em.createQuery("SELECT es FROM EmployeeEntranceState es WHERE es.id = :id"
                + " AND es.deleted = false", EmployeeEntranceState.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public EmployeeEntranceState findByEmployeeAndEntrance(int employeeId, int entranceId) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es "
                + "WHERE es.employee.id = :employeeId "
                + "AND es.entrance.id = :entranceId"
                + " AND es.deleted = false",
                EmployeeEntranceState.class)
                .setParameter("employeeId", employeeId)
                .setParameter("entranceId", entranceId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<EmployeeEntranceState> findByEmployee(int employeeId) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es "
                + "WHERE es.employee.id = :employeeId"
                + " AND es.deleted = false",
                EmployeeEntranceState.class)
                .setParameter("employeeId", employeeId)
                .getResultList();
    }

    // Find all employees at one entrance with specific state
    public List<EmployeeEntranceState> findByEntranceAndState(int entranceId, LocationState state) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es "
                + "WHERE es.entrance.id = :entranceId "
                + "AND es.currentState = :state"
                + " AND es.deleted = false",
                EmployeeEntranceState.class)
                .setParameter("entranceId", entranceId)
                .setParameter("state", state)
                .getResultList();
    }

    // Get employees currently INSIDE at specific entrance
    public List<EmployeeEntranceState> findEmployeesInsideAtEntrance(int entranceId) {
        return findByEntranceAndState(entranceId, LocationState.INSIDE);
    }

    // Get employees currently OUTSIDE at specific entrance
    public List<EmployeeEntranceState> findEmployeesOutsideAtEntrance(int entranceId) {
        return findByEntranceAndState(entranceId, LocationState.OUTSIDE);
    }

    public EmployeeEntranceState recordEntryOrExit(Employee employee, Entrances entrance, DevicePosition position, String updatedBy, Devices device) {
        LocationState newState = (position == DevicePosition.ENTRY)
                ? LocationState.INSIDE
                : LocationState.OUTSIDE;

        String reason = (position == DevicePosition.ENTRY)
                ? "STRICT entry granted"
                : "STRICT exit granted";

        EmployeeEntranceState existingState = findByEmployeeAndEntrance(employee.getId(), entrance.getId());

        if (existingState == null) {
            // First time, create new record
            EmployeeEntranceState newStateRecord = new EmployeeEntranceState(employee, entrance, newState, device);
            newStateRecord.updateState(newState, updatedBy, reason);
            em.persist(newStateRecord);

            logStateChange(newStateRecord, newState, updatedBy, reason);
            return newStateRecord;
        }

        // Update existing record
        existingState.updateState(newState, updatedBy, reason);
        logStateChange(existingState, newState, updatedBy, reason);
        return em.merge(existingState);
    }

    // Admin reset - force set state
    public EmployeeEntranceState resetEmployeeState(int employeeId, int entranceId, LocationState newState, String adminUser, String resetReason, Devices device) {
        System.out.println("Admin reset for employee: " + employeeId + " at entrance: " + entranceId);

        EmployeeEntranceState existingState = findByEmployeeAndEntrance(employeeId, entranceId);

        if (existingState != null) {
            String reason = "Admin reset: " + (resetReason != null ? resetReason : "No reason provided");
            existingState.updateState(newState, adminUser, reason);

            EmployeeEntranceState updated = em.merge(existingState);
            logStateChange(existingState, newState, adminUser, reason);
            em.flush();

            System.out.println("Admin " + adminUser + " reset state for employee: " + employeeId + " to: " + newState);
            return updated;
        } else {
            // Create new state if doesn't exist

            Employee employee = employeeService.findEmployeeById(employeeId);
            Entrances entrance = entranceService.findEntranceByIdFresh(entranceId);

            if (employee == null || entrance == null) {
                throw new EntityNotFoundException("Employee or Entrance not found");
            }
            EmployeeEntranceState newStateRecord = new EmployeeEntranceState(employee, entrance, newState , device);
            String reason = "Admin reset (new): " + (resetReason != null ? resetReason : "No reason provided");
            newStateRecord.updateState(newState, adminUser, reason);
            logStateChange(newStateRecord, newState, adminUser, reason);

            em.persist(newStateRecord);
            em.flush();

            System.out.println("Admin " + adminUser + " created new state for employee: " + employee.getFirstname() + " as: " + newState);
            return newStateRecord;
        }
    }

    // Count employees inside at entrance
    public Long countEmployeesInside(int entranceId) {
        return em.createQuery(
                "SELECT COUNT(es) FROM EmployeeEntranceState es "
                + "WHERE es.entrance.id = :entranceId "
                + "AND es.currentState = :state"
                + " AND es.deleted = falsel", Long.class)
                .setParameter("entranceId", entranceId)
                .setParameter("state", LocationState.INSIDE)
                .getSingleResult();
    }

    private void logStateChange(EmployeeEntranceState sourceState, LocationState newState, String updatedBy, String reason) {
        EmployeeEntranceStateLog log = new EmployeeEntranceStateLog();
        log.setEmployee(sourceState.getEmployee());
        log.setEntrance(sourceState.getEntrance());
        log.setState(newState);
        log.setUpdatedBy(updatedBy);
        log.setReason(reason);
        log.setCreatedDate(LocalDateTime.now());
        log.setMode(sourceState.getEntrance().getEntranceMode());
        log.setDevice(sourceState.getDeviceUsed());
        em.persist(log);
    }
}
