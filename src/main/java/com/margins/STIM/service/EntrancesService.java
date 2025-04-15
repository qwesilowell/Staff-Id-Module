/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Entrances;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.faces.context.FacesContext;
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

    @PersistenceContext(name = "STIM_persistence_unit")
    private EntityManager entityManager;

    /**
     * Create a new entrance.
     *
     * @param entrance The entrance to create.
     * @return The created entrance.
     */
    public Entrances createEntrance(Entrances entrance) {
        entityManager.persist(entrance);
        return entrance;
    }

    /**
     * Retrieve all entrances.
     *
     * @return A list of all entrances.
     */
    public List<Entrances> findAllEntrances() {
        return entityManager.createQuery("SELECT e FROM Entrances e", Entrances.class).getResultList();
    }

    /**
     * Retrieve an entrance by ID.
     *
     * @param id The ID of the entrance.
     * @return The entrance, or null if not found.
     */
    public Entrances findEntranceById(String id) {
        return entityManager.find(Entrances.class, id);
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
    public Entrances updateEntrance(String id, Entrances entrance) {
        Entrances existingEntrance = entityManager.find(Entrances.class, id);
        if (existingEntrance != null) {
            existingEntrance.setEntrance_Device_ID(entrance.getEntrance_Device_ID());
            existingEntrance.setEntrance_Name(entrance.getEntrance_Name());
            existingEntrance.setEntrance_Location(entrance.getEntrance_Location());
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
    public void deleteEntrance(String id) {
        Entrances entrance = entityManager.find(Entrances.class, id);
        if (entrance != null) {
            entityManager.remove(entrance);
        } else {
            throw new EntityNotFoundException("Entrance with ID " + id + " not found.");
        }
    }


    public List<Entrances> searchEntrances(String query) {
        return entityManager.createQuery("SELECT e FROM Entrances e WHERE LOWER(e.entrance_Name) LIKE :query OR e.entrance_Device_ID LIKE :query", Entrances.class)
                .setParameter("query", "%" + query.toLowerCase() + "%")
                .getResultList();
    }

    public Entrances save(Entrances entrance) {
        return entityManager.merge(entrance);
    }

    public void addEntrance(Entrances entrance) {
        entityManager.persist(entrance);
    }
    
    public boolean canAccessEntrance(String employeeRoleId, String entranceDeviceId) {
        Entrances entrance = findEntranceById(entranceDeviceId);

        if (entrance == null) {
            throw new RuntimeException("Entrance not found");
        }

        return entrance.getAllowedRoles().stream()
                .anyMatch(role -> role.getId() == Integer.parseInt(employeeRoleId));
    }
    
    public void handleAccessRequest(String employeeRoleId, String entranceDeviceId) {
        if (canAccessEntrance(employeeRoleId, entranceDeviceId)) {
            System.out.println("Access granted.");
            // TODO: Trigger door unlock mechanism
        } else {
            System.out.println("Access denied.");
            // TODO: Trigger security alert
        }
    }
    
  public int getTotalEntrances() {
        Long count = entityManager.createQuery("SELECT COUNT(e) FROM Entrances e", Long.class)
                .getSingleResult();
        return count.intValue();
    }

   
    public List<Entrances> getEntrancesForUser(String username) {
        // Get ghanaCardNumber from session
        String ghanaCardNumber = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("ghanaCardNumber");
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

}
