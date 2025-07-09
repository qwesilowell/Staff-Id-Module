/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.entity.enums.LocationState;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

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
        return em.createQuery("SELECT es FROM EmployeeEntranceState es", EmployeeEntranceState.class)
                .getResultList();
    }

    // Find entrance state by ID
    public EmployeeEntranceState findEmployeeEntranceStateById(Long id) {
        return em.createQuery("SELECT es FROM EmployeeEntranceState es WHERE es.id = :id", EmployeeEntranceState.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    
     public EmployeeEntranceState findByEmployeeAndEntrance(int employeeId, int entranceDeviceId) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es " +
                "WHERE es.employee.id = :employeeId " +
                "AND es.entrance.id = :entranceDeviceId", 
                EmployeeEntranceState.class)
                .setParameter("employeeId", employeeId)
                .setParameter("entranceDeviceId", entranceDeviceId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
     
     public List<EmployeeEntranceState> findByEmployee(int employeeId) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es "
                + "WHERE es.employee.id = :employeeId",
                EmployeeEntranceState.class)
                .setParameter("employeeId", employeeId)
                .getResultList();
    }
     
    // Find all employees at one entrance with specific state
    public List<EmployeeEntranceState> findByEntranceAndState(int entranceDeviceId, LocationState state) {
        return em.createQuery(
                "SELECT es FROM EmployeeEntranceState es "
                + "WHERE es.entrance.id = :entranceDeviceId "
                + "AND es.currentState = :state",
                EmployeeEntranceState.class)
                .setParameter("entranceDeviceId", entranceDeviceId)
                .setParameter("state", state)
                .getResultList();
    }

    // Get employees currently INSIDE at specific entrance
    public List<EmployeeEntranceState> findEmployeesInsideAtEntrance(int entranceDeviceId) {
        return findByEntranceAndState(entranceDeviceId, LocationState.INSIDE);
    }

    // Get employees currently OUTSIDE at specific entrance
    public List<EmployeeEntranceState> findEmployeesOutsideAtEntrance(int entranceDeviceId) {
        return findByEntranceAndState(entranceDeviceId, LocationState.OUTSIDE);
    } 
    
    public EmployeeEntranceState recordStrictEntryOrExit(Employee employee, Entrances entrance, DevicePosition position, String updatedBy) {
        LocationState newState = (position == DevicePosition.ENTRY)
                ? LocationState.INSIDE
                : LocationState.OUTSIDE;

        String reason = (position == DevicePosition.ENTRY)
                ? "STRICT entry granted"
                : "STRICT exit granted";

        EmployeeEntranceState existingState = findByEmployeeAndEntrance(employee.getId(), entrance.getId());

        if (existingState == null) {
            // First time, create new record
            EmployeeEntranceState newStateRecord = new EmployeeEntranceState(employee, entrance, newState);
            newStateRecord.updateState(newState, updatedBy, reason);
            em.persist(newStateRecord);
            return newStateRecord;
        }

        // Update existing record
        existingState.updateState(newState, updatedBy, reason);
        return em.merge(existingState);
    }

    // Admin reset - force set state
    public EmployeeEntranceState resetEmployeeState(int employeeId, int entranceDeviceId,
            LocationState newState, String adminUser, String resetReason) {
        System.out.println("Admin reset for employee: " + employeeId + " at entrance: " + entranceDeviceId);

        EmployeeEntranceState existingState = findByEmployeeAndEntrance(employeeId, entranceDeviceId);

        if (existingState != null) {
            String reason = "Admin reset: " + (resetReason != null ? resetReason : "No reason provided");
            existingState.updateState(newState, adminUser, reason);

            EmployeeEntranceState updated = em.merge(existingState);
            em.flush();

            System.out.println("Admin " + adminUser + " reset state for employee: " + employeeId + " to: " + newState);
            return updated;
        } else {
            // Create new state if doesn't exist
            
            Employee employee = employeeService.findEmployeeById(employeeId);
            Entrances entrance = entranceService.findEntranceByIdFresh(entranceDeviceId);

            if (employee == null || entrance == null) {
                throw new EntityNotFoundException("Employee or Entrance not found");
            }
            EmployeeEntranceState newStateRecord = new EmployeeEntranceState(employee, entrance, newState);
            String reason = "Admin reset (new): " + (resetReason != null ? resetReason : "No reason provided");
            newStateRecord.updateState(newState, adminUser, reason);

            em.persist(newStateRecord);
            em.flush();

            System.out.println("Admin " + adminUser + " created new state for employee: " + employee.getFirstname() + " as: " + newState);
            return newStateRecord;
        }
    }

    // Count employees inside at entrance
    public Long countEmployeesInside(int entranceDeviceId) {
        return em.createQuery(
                "SELECT COUNT(es) FROM EmployeeEntranceState es "
                + "WHERE es.entrance.id = :entranceDeviceId "
                + "AND es.currentState = :state", Long.class)
                .setParameter("entranceDeviceId", entranceDeviceId)
                .setParameter("state", LocationState.INSIDE)
                .getSingleResult();
    }
    
    
    // Create new entrance state
//    public EmployeeEntranceState saveEmployeeEntranceState(EmployeeEntranceState state) {
//        em.persist(state);
//        return state;
//    }
//
//    public EmployeeEntranceState updateEmployeeEntranceState(Long id, EmployeeEntranceState state) {
//        EmployeeEntranceState existingState = findEmployeeEntranceStateById(id);
//        if (existingState != null) {
//            state.setId(id); // Ensure ID consistency
//            return em.merge(state);
//        }
//        throw new EntityNotFoundException("Employee entrance state does not exist with name: " + state.getEmployee());
//    }
}
