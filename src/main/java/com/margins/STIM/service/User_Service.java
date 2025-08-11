/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.SystemUserRoles;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.entity.enums.PagePermission;
import com.margins.STIM.entity.enums.UserType;
import com.margins.STIM.util.JSF;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class User_Service {

    @PersistenceContext(name = "STIM_persistence_unit")
    EntityManager em;

    //Create
    public Users createUser(Users user) {
        //Persist into db
        em.persist(user);
        return user;
    }

    public String createSystemUser(SystemUserRoles userRole) {
        try {
            // Check if system user already exists
            Users existingUser = findUserByGhanaCard("GHA-726682342-4");
            if (existingUser != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "System user already exists!", null));
                return "User already Exists";
            }

            Users systemUser = new Users();
            systemUser.setUsername("PHILIP MANTE ASARE");
            systemUser.setUserType(UserType.ADMIN);
            systemUser.setGhanaCardNumber("GHA-726682342-4");
            systemUser.setUserRole(userRole);
            createUser(systemUser);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "System user created successfully!", null));

            return "User created succesfully"; // used to be return null

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating system user: " + e.getMessage(), null));
            e.printStackTrace();
            return null;
        }
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
    public Users updateUser(Users updatedUser) {
        Users existingUser = findUserById(updatedUser.getId());
        if (existingUser != null) {
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setUserType(updatedUser.getUserType());
            existingUser.setUserRole(updatedUser.getUserRole());
            existingUser.setStatus(updatedUser.getStatus());
            em.merge(existingUser);
            return existingUser;
        }
        throw new EntityNotFoundException("User: " + updatedUser.getGhanaCardNumber() + " not found.");
    }

    /**
     * Find a user by ID.
     *
     * @param ghanaCardNumber The ID of the user to find.
     * @return The found user or null if not found.
     */
    public Users findUserByGhanaCard(String ghanaCardNumber) {
        return em.createQuery("SELECT u FROM Users u WHERE UPPER(u.ghanaCardNumber) = UPPER(:ghanaCardNumber)"
                + "AND u.deleted = false ORDER BY u.createdAt DESC", Users.class)
                .setParameter("ghanaCardNumber", ghanaCardNumber)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public Users findUserById(int id) {
        return em.find(Users.class, id);
    }

    public List<Users> findAllUsersByGhanaCard(String ghanaCardPattern) {
        return em.createQuery(
                "SELECT u FROM Users u WHERE LOWER(u.ghanaCardNumber) LIKE :ghanaCardPattern"
                + "AND u.deleted = false ORDER BY u.createdAt DESC ", Users.class)
                .setParameter("ghanaCardPattern", "%" + ghanaCardPattern.toLowerCase() + "%")
                .getResultList();
    }

    /**
     * Retrieve all users.
     *
     * @return A list of all users.
     */
    public List<Users> findAllUsers() {
        return em.createQuery("SELECT u FROM Users u WHERE u.deleted = false ORDER BY u.createdAt DESC", Users.class)
                .getResultList();
    }

    public List<Users> findUsersByUsernameLike(String query) {
        return em.createQuery("SELECT u FROM Users u WHERE LOWER(u.username) LIKE :query", Users.class)
                .setParameter("query", "%" + query.toLowerCase() + "%")
                .setMaxResults(20) // limit results for performance
                .getResultList();
    }

    /**
     * Delete a user by ID.
     *
     * @param users The ID of the user to delete.
     * @throws EntityNotFoundException if the user with the given ID is not
     * found.
     */
    public void deleteUser(Users users) {
        Users user = findUserById(users.getId());
        if (user != null) {
            user.setDeleted(true);
            em.merge(user);
        } else {
            throw new EntityNotFoundException("User not found.");
        }
    }

    public Users getCurrentUser() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String username = (String) externalContext.getSessionMap().get("username");

        if (username == null || username.isEmpty()) {
            return null;
        }

        try {
            return em.createQuery("SELECT u FROM Users u WHERE LOWER(u.username) = LOWER(:username) And u.deleted = false", Users.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    //for ViewPermission
    public ViewPermission findByPageName(String pageName) {
        try {
            return em.createQuery("SELECT v FROM ViewPermission v WHERE v.pagePath = :page", ViewPermission.class)
                    .setParameter("page", pageName)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public ViewPermission findPageById(int id) {
        return em.find(ViewPermission.class, id);
    }

    public void saveVp(ViewPermission vp) {
        em.persist(vp);
    }

    public List<ViewPermission> findAll() {
        return em.createQuery("SELECT v FROM ViewPermission v", ViewPermission.class).getResultList();
    }

    public void seedViewPermissions() {
        for (PagePermission pp : PagePermission.values()) {
            // Check if this page already exists in DB
            ViewPermission existing = findByPageName(pp.getPagePath());
            if (existing == null) {
                ViewPermission vp = new ViewPermission();
                vp.setPagePath(pp.getPagePath());
                vp.setDisplayName(pp.getDisplayName());
                saveVp(vp);
                JSF.addSuccessMessage(findAll().size() + " pages added succesfully");
            }
        }
    }
}

//    // Validate user credentials
//    public boolean validateUser(String ghanaCardNumber, String password) {
//        try {
//            Users user = em.createQuery(
//                    "SELECT u FROM Users u WHERE u.ghanaCardNumber = :ghanaCardNumber", Users.class)
//                    .setParameter("ghanaCardNumber", ghanaCardNumber)
//                    .getSingleResult();
//
//            if (user != null && BCrypt.checkpw(password, user.getPassword())) { // Hash check
//                return "Admin".equals(user.getUserRole()); // Return true only if role is Admin
//            }
//        } catch (NoResultException e) {
//            return false; // User not found
//        }
//        return false; // Password does not match or not an Admin
//    }
//}
