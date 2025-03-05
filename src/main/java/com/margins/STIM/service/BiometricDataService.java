/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.BiometricData;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 * Service class for managing BiometricData.
 * 
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class BiometricDataService {

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Create a new biometric data record.
     *
     * @param biometricData The biometric data to save.
     * @return The created BiometricData object.
     */
    public BiometricData createBiometricData(BiometricData biometricData) {
        entityManager.persist(biometricData);
        return biometricData;
    }

    /**
     * Retrieve a biometric data record by its ID.
     *
     * @param id The ID of the biometric data to retrieve.
     * @return The BiometricData object, or null if not found.
     */
    public BiometricData findBiometricDataById(Long id) {
        BiometricData biometricData = entityManager.find(BiometricData.class, id);
        if (biometricData == null) {
            throw new EntityNotFoundException("BiometricData with ID " + id + " not found.");
        }
        return biometricData;
    }
    /**
     * Retrieve all biometric data records.
     *
     * @return A list of all BiometricData objects.
     */
    public List<BiometricData> findAllBiometricData() {
        return entityManager.createQuery("SELECT b FROM BiometricData b", BiometricData.class).getResultList();
    }

    /**
     * Update an existing biometric data record.
     *
     * @param id           The ID of the biometric data to update.
     * @param updatedData  The updated biometric data.
     * @return The updated BiometricData object.
     * @throws EntityNotFoundException if the biometric data with the given ID is not found.
     */
    public BiometricData updateBiometricData(Long id, BiometricData updatedData) {
        BiometricData existingData = entityManager.find(BiometricData.class, id);
        if (existingData == null) {
            throw new EntityNotFoundException("BiometricData with ID " + id + " not found.");
        }
        existingData.setFingerprintData(updatedData.getFingerprintData());
        entityManager.merge(existingData);
        return existingData;
    }

    /**
     * Delete a biometric data record by its ID.
     *
     * @param id The ID of the biometric data to delete.
     * @throws EntityNotFoundException if the biometric data with the given ID is not found.
     */
    public void deleteBiometricData(Long id) {
        BiometricData biometricData = entityManager.find(BiometricData.class, id);
        if (biometricData == null) {
            throw new EntityNotFoundException("BiometricData with ID " + id + " not found.");
        }
        entityManager.remove(biometricData);
    }
}
