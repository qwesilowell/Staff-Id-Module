/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Access_Levels;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntrance;
import com.margins.STIM.entity.Entrances;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
public class EmployeeEntranceService {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    // Create a new EmployeeEntrance
    public EmployeeEntrance createEmployeeEntrance(EmployeeEntrance employeeEntrance) {
        // Validate and fetch existing Employee
        Employee existingEmployee = entityManager.find(Employee.class, employeeEntrance.getEmployee().getGhanaCardNumber());
        if (existingEmployee == null) {
            throw new IllegalArgumentException("Employee with GhanaCardNumber " + employeeEntrance.getEmployee().getGhanaCardNumber() + " does not exist.");
        }

        // Validate and fetch existing Entrance
        Entrances existingEntrance = entityManager.find(Entrances.class, employeeEntrance.getEntrance().getEntrance_Device_ID());
        if (existingEntrance == null) {
            throw new IllegalArgumentException("Entrance with ID " + employeeEntrance.getEntrance().getEntrance_Device_ID() + " does not exist.");
        }

        // Validate and fetch existing Access Level
        Access_Levels existingAccessLevel = entityManager.find(Access_Levels.class, employeeEntrance.getAccessLevel().getId());
        if (existingAccessLevel == null) {
            throw new IllegalArgumentException("Access Level with ID " + employeeEntrance.getAccessLevel().getId() + " does not exist.");
        }

        // Set existing relationships to avoid creating new entities
        employeeEntrance.setEmployee(existingEmployee);
        employeeEntrance.setEntrance(existingEntrance);
        employeeEntrance.setAccessLevel(existingAccessLevel);

        // Persist the EmployeeEntrance
        entityManager.persist(employeeEntrance);
        return employeeEntrance;
    }

    // Retrieve all EmployeeEntrance records
    public List<EmployeeEntrance> getAllEmployeeEntrances() {
        return entityManager.createQuery("SELECT ee FROM EmployeeEntrance ee", EmployeeEntrance.class).getResultList();
    }

    // Retrieve EmployeeEntrance records by ghanaCardNumber
    public List<EmployeeEntrance> getEmployeeEntrancesByGhanaCardNumber(String ghanaCardNumber) {
        return entityManager.createQuery(
                "SELECT ee FROM EmployeeEntrance ee WHERE ee.employee.ghanaCardNumber = :ghanaCardNumber",
                EmployeeEntrance.class)
                .setParameter("ghanaCardNumber", ghanaCardNumber)
                .getResultList();
    }
    // Retrieve assigned entrances for an employee (alias for getEmployeeEntrancesByGhanaCardNumber)
    public List<EmployeeEntrance> getAssignedEntrances(String ghanaCardNumber) {
        return getEmployeeEntrancesByGhanaCardNumber(ghanaCardNumber);
    }

    // Update access level for an EmployeeEntrance record
    public EmployeeEntrance updateAccessLevel(Long id, String newAccessLevel) {
        EmployeeEntrance ee = entityManager.find(EmployeeEntrance.class, id);
        if (ee != null) {
            ee.getAccessLevel().setLevel_name(newAccessLevel);
            entityManager.merge(ee);
        }
        return ee;
    }

    // Delete an EmployeeEntrance record by ID
    public void deleteEmployeeEntrance(Long id) {
        EmployeeEntrance ee = entityManager.find(EmployeeEntrance.class, id);
        if (ee != null) {
            entityManager.remove(ee);
        }
    }
    
//    public List<EmployeeEntrance> getAssignedEntrances(String ghanaCardNumber) {
//        if (ghanaCardNumber == null || ghanaCardNumber.trim().isEmpty()) {
//            throw new IllegalArgumentException("Ghana Card Number cannot be null or empty.");
//        }
//
//        return entityManager.createQuery(
//                "SELECT ee FROM EmployeeEntrance ee WHERE ee.employee.ghanaCardNumber = :ghanaCardNumber",
//                EmployeeEntrance.class)
//                .setParameter("ghanaCardNumber", ghanaCardNumber)
//                .getResultList();
//    }

    public boolean canAccessEntrance(String employeeGhanaCardNumber, String entranceDeviceId) {
        try {
            EmployeeEntrance employeeEntrance = entityManager.createQuery(
                    "SELECT ee FROM EmployeeEntrance ee WHERE ee.employee.ghanaCardNumber = :employeeId AND ee.entrance.entranceDeviceId= :entranceId",
                    EmployeeEntrance.class)
                    .setParameter("employeeId", employeeGhanaCardNumber)
                    .setParameter("entranceId", entranceDeviceId)
                    .getSingleResult();

            return employeeEntrance.getAccessLevel().getIsAllowed(); // Grant/Deny access based on access level

        } catch (NoResultException e) {
            return false; // If no access level is found, deny access
        }
    }
    
    public void saveEmployeeEntrances(Employee employee, List<EmployeeEntrance> assignedEntrances) {
        if (employee == null || assignedEntrances == null || assignedEntrances.isEmpty()) {
            throw new IllegalArgumentException("Employee and assigned entrances cannot be null or empty.");
        }

        for (EmployeeEntrance entrance : assignedEntrances) {
            boolean alreadyAssigned = getAssignedEntrances(employee.getGhanaCardNumber()).stream()
                    .anyMatch(e -> e.getEntrance().getEntrance_Device_ID().equals(entrance.getEntrance().getEntrance_Device_ID()));

            if (!alreadyAssigned) {
                createEmployeeEntrance(entrance);
            }
        }
    }

    public void updateEmployeeEntrance(EmployeeEntrance entry) {
        EmployeeEntrance existingRecord = entityManager.find(EmployeeEntrance.class, entry.getId());

        if (existingRecord != null) {
            existingRecord.setAccessLevel(entry.getAccessLevel()); // Update access level
            existingRecord.setEntrance(entry.getEntrance()); // Ensure entrance is updated
            entityManager.merge(existingRecord);
        } else {
            throw new IllegalArgumentException("Employee entrance record not found for update.");
        }
    }


    public void updateEmployeeEntrances(Employee employee, List<EmployeeEntrance> assignedEntrances) {
        if (employee == null || assignedEntrances == null || assignedEntrances.isEmpty()) {
            throw new IllegalArgumentException("Employee and assigned entrances cannot be null or empty.");
        }

        for (EmployeeEntrance entrance : assignedEntrances) {
            EmployeeEntrance existingRecord = getAssignedEntrances(employee.getGhanaCardNumber()).stream()
                    .filter(e -> e.getEntrance().getEntrance_Device_ID().equals(entrance.getEntrance().getEntrance_Device_ID()))
                    .findFirst()
                    .orElse(null);

            if (existingRecord != null) {
                // Update only the Access Level if entrance is already assigned
                existingRecord.setAccessLevel(entrance.getAccessLevel());
                entityManager.merge(existingRecord);
            } else {
                // If entrance is not already assigned, create a new record
                createEmployeeEntrance(entrance);
            }
        }
    }

}
