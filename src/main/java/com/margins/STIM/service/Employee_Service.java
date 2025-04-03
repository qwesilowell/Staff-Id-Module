/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.BiometricData;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.EmploymentStatus;
import java.util.List;
import java.util.regex.Pattern;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 *
 * @author PhilipManteAsare
 */

@Stateless
@Transactional
public class Employee_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    // Create 
    public Employee saveEmployee(Employee employee) {
         // New employee
        entityManager.persist(employee);
        return employee;
    }
    //Update 
    public Employee updateEmployee(String ghanaCardNumber, Employee employee) { 
        Employee existingEmployee = findEmployeeByGhanaCard(ghanaCardNumber);
        if (existingEmployee != null) {
            employee.setGhanaCardNumber(ghanaCardNumber); // Ensure Ghana Card number consistency
            return entityManager.merge(employee);
        }
        throw new EntityNotFoundException("Employee does not exist with Ghana Card number: " + ghanaCardNumber);
    }

    // Retrieve all Employees
    public List<Employee> findAllEmployees() {
        return entityManager.createQuery("SELECT e FROM Employee e", Employee.class)
                .getResultList();
    }

    // Find Employee by Ghana Card Number
     public Employee findEmployeeByGhanaCard(String ghanaCardNumber) {
        return entityManager.createQuery("SELECT e FROM Employee e WHERE e.ghanaCardNumber = :ghanaCardNumber", Employee.class)
                .setParameter("ghanaCardNumber", ghanaCardNumber)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

     // Search Employees by partial Ghana Card Number
    public List<Employee> searchEmployeesByGhanaCard(String ghanaCardPattern) {
        return entityManager.createQuery("SELECT e FROM Employee e WHERE LOWER(e.ghanaCardNumber) LIKE :ghanaCardPattern", Employee.class)
        .setParameter("ghanaCardPattern", "%" + ghanaCardPattern.toLowerCase() + "%")
        .getResultList();
    }
    
        // Method to validate Ghana Card number format
    public boolean validateGhanaCard(String ghanaCardNumber) {
        if (ghanaCardNumber == null || ghanaCardNumber.trim().isEmpty()) {
            return false;
        }

        // Ghana Card format: GHA-XXXXXXXXX-X
        // GHA followed by 9 digits, then a hyphen, and a single digit at the end.
        Pattern pattern = Pattern.compile("^GHA-\\d{9}-\\d$");
        return pattern.matcher(ghanaCardNumber).matches();
    }

    
    // Delete Employee by Ghana Card Number
    public void deleteEmployee(String ghanaCardNumber) {
        Employee employee = findEmployeeByGhanaCard(ghanaCardNumber);
        if (employee != null) {
            entityManager.remove(employee);
        } else {
            throw new EntityNotFoundException("Employee does not exist with Ghana Card number: " + ghanaCardNumber);
        }
    }
     public void registerEmployee(Employee employee, BiometricData biometricData) {
        try {
            entityManager.persist(employee); // Save employee WITHOUT role

            if (biometricData != null) {
                entityManager.persist(biometricData);
            }

            entityManager.flush(); // Commit changes immediately

        } catch (Exception e) {
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

     
     
    public void assignRoleToEmployee(String ghanaCardNumber, EmployeeRole role) {
        Employee employee = findEmployeeByGhanaCard(ghanaCardNumber);

        if (employee != null) {
            employee.setRole(role); // Assign role here
            entityManager.merge(employee); // Update the database

            entityManager.flush(); // Ensure changes are saved
        } else {
            throw new EntityNotFoundException("Employee with Ghana Card number " + ghanaCardNumber + " not found.");
        }
    }
    
    public Employee findEmployeeByName(String name) {
        return entityManager.createQuery(
                "SELECT e FROM Employee e WHERE LOWER(e.firstname) LIKE :name OR LOWER(e.lastname) LIKE :name", Employee.class)
                .setParameter("name", "%" + name.toLowerCase() + "%") // Case-insensitive search
                .getResultStream().findFirst().orElse(null);
    }
    
    public List<Employee> searchEmployees(String searchQuery) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return findAllEmployees(); // Return all employees if search query is empty
        }

        // Search by both name and Ghana Card number
        String query = searchQuery.toLowerCase();
        return entityManager.createQuery(
                "SELECT e FROM Employee e WHERE "
                + "LOWER(e.firstname) LIKE :query OR "
                + "LOWER(e.lastname) LIKE :query OR "
                + "LOWER(e.ghanaCardNumber) LIKE :query", Employee.class)
                .setParameter("query", "%" + query + "%")
                .getResultList();
    }
    
    
    // Create
    public EmploymentStatus saveEmploymentStatus(EmploymentStatus status) {
        entityManager.persist(status);
        return status;
    }

    // Update
    public EmploymentStatus updateEmploymentStatus(int statusId, EmploymentStatus status) {
        EmploymentStatus existingStatus = findEmploymentStatusById(statusId);
        if (existingStatus != null) {
            status.setId(statusId); // Ensure ID consistency
            return entityManager.merge(status);
        }
        throw new EntityNotFoundException("Employment status does not exist with ID: " + statusId);
    }

    // Retrieve all
    public List<EmploymentStatus> findAllEmploymentStatuses() {
        return entityManager.createQuery("SELECT es FROM EmploymentStatus es", EmploymentStatus.class)
                .getResultList();
    }

    // Find by ID
    public EmploymentStatus findEmploymentStatusById(int statusId) {
        return entityManager.createQuery("SELECT es FROM EmploymentStatus es WHERE es.id = :statusId", EmploymentStatus.class)
                .setParameter("statusId", statusId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    // Delete
    public void deleteEmploymentStatus(int statusId) {
        EmploymentStatus status = findEmploymentStatusById(statusId);
        if (status != null) {
            if (isEmploymentStatusInUse(statusId)) {
                throw new IllegalStateException("Cannot delete employment status in use by employees.");
            }
            entityManager.remove(status);
        } else {
            throw new EntityNotFoundException("Employment status does not exist with ID: " + statusId);
        }
    }

    // Helper to check if status is in use
    public boolean isEmploymentStatusInUse(int statusId) {
        Long count = entityManager.createQuery("SELECT COUNT(e) FROM Employee e WHERE e.employmentStatus.id = :statusId", Long.class)
                .setParameter("statusId", statusId)
                .getSingleResult();
        return count > 0;
    }
}


