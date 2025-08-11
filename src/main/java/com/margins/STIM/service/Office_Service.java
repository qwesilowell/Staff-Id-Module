/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Office;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Service class for managing Office entities.
 * Provides methods for creating, updating, retrieving, and deleting offices.
 * 
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class Office_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Create a new office.
     *
     * @param office The office entity to create.
     * @return The created office entity.
     */
    public Office createOffice(Office office) {
        entityManager.persist(office);
        return office;
    }

    /**
     * Update an existing office.
     *
     * @param officeId The ID of the office to update.
     * @param updatedOffice The updated office entity.
     * @return The updated office entity.
     * @throws EntityNotFoundException if the office with the given ID is not found.
     */
    public Office updateOffice(Long officeId, Office updatedOffice) {
        Office existingOffice = entityManager.find(Office.class, officeId);
        if (existingOffice != null) {
            existingOffice.setOfficeName(updatedOffice.getOfficeName());
            existingOffice.setCity(updatedOffice.getCity());
            existingOffice.setState(updatedOffice.getState());
            existingOffice.setCountry(updatedOffice.getCountry());
            entityManager.merge(existingOffice);
            return existingOffice;
        }
        throw new EntityNotFoundException("Office with ID " + officeId + " not found.");
    }

    /**
     * Find an office by ID.
     *
     * @param officeId The ID of the office to find.
     * @return The found office entity, or null if not found.
     */
    public Office findOfficeById(Long officeId) {
        return entityManager.find(Office.class, officeId);
    }

    /**
     * Retrieve all offices.
     *
     * @return A list of all office entities.
     */
    public List<Office> findAllOffices() {
        return entityManager.createQuery("SELECT o FROM Office o", Office.class).getResultList();
    }

    /**
     * Delete an office by ID.
     *
     * @param officeId The ID of the office to delete.
     * @throws EntityNotFoundException if the office with the given ID is not found.
     */
    public void deleteOffice(Long officeId) {
        Office office = entityManager.find(Office.class, officeId);
        if (office != null) {
            office.setDeleted(true);
            entityManager.merge(office);
        } else {
            throw new EntityNotFoundException("Office with ID " + officeId + " not found.");
        }
    }
}
