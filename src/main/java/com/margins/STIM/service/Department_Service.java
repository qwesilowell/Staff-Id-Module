/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Department;
import com.margins.STIM.entity.Office;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Service class for managing Department entities.
 * Provides methods for creating, updating, retrieving, and deleting departments.
 * 
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class Department_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Create a new department.
     *
     * @param department The department entity to create.
     * @return The created department entity.
     */
    public Department createDepartment(Department department) {
        entityManager.persist(department);
        return department;
    }

    /**
     * Update an existing department.
     *
     * @param departmentId The ID of the department to update.
     * @param updatedDepartment The updated department entity.
     * @return The updated department entity.
     * @throws EntityNotFoundException if the department with the given ID is not found.
     */
    public Department updateDepartment(Long departmentId, Department updatedDepartment) {
        Department existingDepartment = entityManager.find(Department.class, departmentId);
        if (existingDepartment != null) {
            existingDepartment.setDepartmentName(updatedDepartment.getDepartmentName());
            existingDepartment.setDepartmentHead(updatedDepartment.getDepartmentHead());
            existingDepartment.setOffice(updatedDepartment.getOffice());
            entityManager.merge(existingDepartment);
            return existingDepartment;
        }
        throw new EntityNotFoundException("Department with ID " + departmentId + " not found.");
    }

    /**
     * Find a department by ID.
     *
     * @param departmentId The ID of the department to find.
     * @return The found department entity, or null if not found.
     */
    public Department findDepartmentById(Long departmentId) {
        return entityManager.find(Department.class, departmentId);
    }

    /**
     * Retrieve all departments.
     *
     * @return A list of all department entities.
     */
    public List<Department> findAllDepartments() {
        return entityManager.createQuery("SELECT d FROM Department d", Department.class).getResultList();
    }

    /**
     * Delete a department by ID.
     *
     * @param departmentId The ID of the department to delete.
     * @throws EntityNotFoundException if the department with the given ID is not found.
     */
    public void deleteDepartment(Long departmentId) {
        Department department = entityManager.find(Department.class, departmentId);
        if (department != null) {
            entityManager.remove(department);
        } else {
            throw new EntityNotFoundException("Department with ID " + departmentId + " not found.");
        }
    }

    /**
     * Find all departments in a specific office.
     *
     * @param office The office for which departments are retrieved.
     * @return A list of departments belonging to the specified office.
     */
    public List<Department> findDepartmentsByOffice(Office office) {
        return entityManager.createQuery("SELECT d FROM Department d WHERE d.office = :office", Department.class)
                .setParameter("office", office)
                .getResultList();
    }
}
