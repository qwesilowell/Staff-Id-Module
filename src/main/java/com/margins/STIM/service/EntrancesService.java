/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.Bean.UserSession;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.entity.enums.EntranceMode;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class EntrancesService {

    @Inject
    private UserSession userSession;

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Retrieve all entrances.
     *
     * @return A list of all entrances.
     */
    public List<Entrances> findAllEntrances() {
        return entityManager.createQuery("SELECT e FROM Entrances e WHERE e.deleted = false ORDER BY e.createdAt DESC ", Entrances.class).getResultList();
    }

    /**
     * Retrieve an entrance by ID.
     *
     * @param id The ID of the entrance.
     * @return The entrance, or null if not found.
     */
    public Entrances findEntranceById(int id) {
        return entityManager.find(Entrances.class, id);
    }

    public Entrances findEntranceByIdFresh(int id) {
        return entityManager.createQuery(
                "SELECT e FROM Entrances e LEFT JOIN FETCH e.employees WHERE e.id = :id",
                Entrances.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public List<Entrances> findEntranceByIds(List<Integer> ids) {
        return entityManager.createQuery(
                "SELECT e FROM Entrances e WHERE e.id IN :ids", Entrances.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    /**
     * Update an existing entrance.
     *
     * @param id The ID of the entrance to update.
     * @param entrance The updated entrance details.
     * @return The updated entrance.
     * @throws EntityNotFoundException if the entrance with the given ID is not
     * found.
     */
    public Entrances updateEntrance(int id, Entrances entrance) {
        Entrances existingEntrance = entityManager.find(Entrances.class, id);
        if (existingEntrance != null) {
            existingEntrance.setEntranceDeviceId(entrance.getEntranceDeviceId());
            existingEntrance.setEntranceName(entrance.getEntranceName());
            existingEntrance.setEntranceLocation(entrance.getEntranceLocation());
            existingEntrance.setEntranceMode(entrance.getEntranceMode());
            entityManager.merge(existingEntrance);
            return existingEntrance;
        }
        throw new EntityNotFoundException("Entrance with ID " + id + " not found.");
    }

    /**
     * Delete an entrance by ID.
     *
     * @param id The ID of the entrance to delete.
     * @throws EntityNotFoundException if the entrance with the given ID is not
     * found.
     */
    public void deleteEntrance(int id) {
        Entrances entrance = entityManager.find(Entrances.class, id);
        if (entrance != null) {
            entrance.setDeleted(true);
            entityManager.merge(entrance);
        } else {
            throw new EntityNotFoundException("Entrance with ID " + id + " not found.");
        }
    }

    public List<Entrances> searchEntrances(String query) {
        return entityManager.createQuery("SELECT e FROM Entrances e WHERE LOWER(e.entranceName) LIKE :query OR e.entranceDeviceId "
                + "LIKE :query", Entrances.class)
                .setParameter("query", "%" + query.toLowerCase() + "%")
                .getResultList();
    }

    public Entrances save(Entrances entrance) {
        return entityManager.merge(entrance);
    }

    public void addEntrance(Entrances entrance) {
        entityManager.persist(entrance);
    }

    public int getTotalEntrances() {
        Long count = entityManager.createQuery("SELECT COUNT(e) FROM Entrances e", Long.class)
                .getSingleResult();
        return count.intValue();
    }

    public List<Entrances> getEntrancesForUser(String ghanaCardNumber) {
        // Get ghanaCardNumber from session
        if (ghanaCardNumber == null) {
            return List.of(); // Return empty list if no ghanaCardNumber
        }

        // Query Employee by ghanaCardNumber
        TypedQuery<Entrances> query = entityManager.createQuery(
                "SELECT DISTINCT e FROM Employee emp "
                + "LEFT JOIN emp.role r "
                + "LEFT JOIN r.accessibleEntrances e "
                + "WHERE emp.ghanaCardNumber = :ghanaCardNumber",
                Entrances.class
        );
        query.setParameter("ghanaCardNumber", ghanaCardNumber);
        return query.getResultList();
    }

    public List<Entrances> findByMode(EntranceMode mode) {
        return entityManager.createQuery("SELECT e FROM Entrances e WHERE e.entranceMode = :mode", Entrances.class)
                .setParameter("mode", mode)
                .getResultList();
    }

    public List<Devices> findDevicesByEntranceAndPosition(Entrances entrance, DevicePosition position) {
        return entityManager.createQuery(
                "SELECT d FROM Devices d "
                + "WHERE d.entrance = :entrance "
                + "AND d.devicePosition = :position "
                + "AND d.deleted = false", Devices.class)
                .setParameter("entrance", entrance)
                .setParameter("position", position)
                .getResultList();
    }
}
