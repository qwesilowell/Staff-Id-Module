/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.Users;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class User_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    EntityManager entityManager;

    //Create
    public Users createUser(Users user) {
        //Persist into db
        entityManager.persist(user);
        return user;
    }

    /**
     * Update an existing user.
     *
     * @param ghanaCardNumber The ID of the user to update.
     * @param updatedUser The updated user data.
     * @return The updated user.
     * @throws EntityNotFoundException if the user with the given ID is not
     * found.
     */
    public Users updateUser(String ghanaCardNumber, Users updatedUser) {
        Users existingUser = entityManager.find(Users.class, ghanaCardNumber);
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setUserRole(updatedUser.getUserRole());
            existingUser.setPassword(updatedUser.getPassword());
            entityManager.merge(existingUser);
            return existingUser;
        }
        throw new EntityNotFoundException("User with ID: " + ghanaCardNumber + " not found.");
    }

    /**
     * Find a user by ID.
     *
     * @param ghanaCardNumber The ID of the user to find.
     * @return The found user or null if not found.
     */
    public Users findUserByGhanaCard(String ghanaCardNumber) {
        return entityManager.find(Users.class, ghanaCardNumber);
    }

    public List<Users> findAllUsersByGhanaCard(String ghanaCardPattern) {
        return entityManager.createQuery(
                "SELECT u FROM Users u WHERE LOWER(u.ghanaCardNumber) LIKE :ghanaCardPattern", Users.class)
                .setParameter("ghanaCardPattern", "%" + ghanaCardPattern.toLowerCase() + "%")
                .getResultList();
    }
    
    /**
     * Retrieve all users.
     *
     * @return A list of all users.
     */
    public List<Users> findAllUsers() {
        return entityManager.createQuery("SELECT u FROM Users u", Users.class).getResultList();
    }

    /**
     * Delete a user by ID.
     *
     * @param ghanaCardNumber The ID of the user to delete.
     * @throws EntityNotFoundException if the user with the given ID is not
     * found.
     */
    public void deleteUser(String ghanaCardNumber) {
//        Users user = entityManager.find(Users.class, ghanaCardNumber);
        Users user = findUserByGhanaCard(ghanaCardNumber);
        if (user != null) {
            entityManager.remove(user);
        } else {
            throw new EntityNotFoundException("User with ID: " + ghanaCardNumber + " not found.");
        }
    }

    // Validate user credentials
    public boolean validateUser(String ghanaCardNumber, String password) {
        try {
            Users user = entityManager.createQuery(
                    "SELECT u FROM Users u WHERE u.ghanaCardNumber = :ghanaCardNumber", Users.class)
                    .setParameter("ghanaCardNumber", ghanaCardNumber)
                    .getSingleResult();

            if (user != null && BCrypt.checkpw(password, user.getPassword())) { // Hash check
                return "Admin".equals(user.getUserRole()); // Return true only if role is Admin
            }
        } catch (NoResultException e) {
            return false; // User not found
        }
        return false; // Password does not match or not an Admin
    }
}
