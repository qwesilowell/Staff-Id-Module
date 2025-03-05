/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Department;
import com.margins.STIM.entity.Sub_Office;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Service class for managing Sub_Office entities.
 * Provides methods for creating, updating, retrieving, and deleting sub-offices.
 * 
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class Sub_Office_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Create a new sub-office.
     *
     * @param subOffice The sub-office entity to create.
     * @return The created sub-office entity.
     */
    public Sub_Office createSubOffice(Sub_Office subOffice) {
        entityManager.persist(subOffice);
        return subOffice;
    }

    /**
     * Update an existing sub-office.
     *
     * @param subOfficeId The ID of the sub-office to update.
     * @param updatedSubOffice The updated sub-office entity.
     * @return The updated sub-office entity.
     * @throws EntityNotFoundException if the sub-office with the given ID is not found.
     */
    public Sub_Office updateSubOffice(Long subOfficeId, Sub_Office updatedSubOffice) {
        Sub_Office existingSubOffice = entityManager.find(Sub_Office.class, subOfficeId);
        if (existingSubOffice != null) {
            existingSubOffice.setSubOfficeName(updatedSubOffice.getSubOfficeName());
            existingSubOffice.setDepartment(updatedSubOffice.getDepartment());
            entityManager.merge(existingSubOffice);
            return existingSubOffice;
        }
        throw new EntityNotFoundException("Sub_Office with ID " + subOfficeId + " not found.");
    }

    /**
     * Find a sub-office by ID.
     *
     * @param subOfficeId The ID of the sub-office to find.
     * @return The found sub-office entity, or null if not found.
     */
    public Sub_Office findSubOfficeById(Long subOfficeId) {
        return entityManager.find(Sub_Office.class, subOfficeId);
    }

    /**
     * Retrieve all sub-offices.
     *
     * @return A list of all sub-office entities.
     */
    public List<Sub_Office> findAllSubOffices() {
        return entityManager.createQuery("SELECT s FROM Sub_Office s", Sub_Office.class).getResultList();
    }

    /**
     * Delete a sub-office by ID.
     *
     * @param subOfficeId The ID of the sub-office to delete.
     * @throws EntityNotFoundException if the sub-office with the given ID is not found.
     */
    public void deleteSubOffice(Long subOfficeId) {
        Sub_Office subOffice = entityManager.find(Sub_Office.class, subOfficeId);
        if (subOffice != null) {
            entityManager.remove(subOffice);
        } else {
            throw new EntityNotFoundException("Sub_Office with ID " + subOfficeId + " not found.");
        }
    }

    /**
     * Find all sub-offices under a specific department.
     *
     * @param department The department for which sub-offices are retrieved.
     * @return A list of sub-offices belonging to the specified department.
     */
    public List<Sub_Office> findSubOfficesByDepartment(Department department) {
        return entityManager.createQuery("SELECT s FROM Sub_Office s WHERE s.department = :department", Sub_Office.class)
                .setParameter("department", department)
                .getResultList();
    }
}
