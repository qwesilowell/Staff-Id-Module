/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.entity.enums.UserStatus;
import com.margins.STIM.entity.enums.UserType;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author PhilipManteAsare
 */
@SessionScoped
@Named("userSession")
public class UserSession implements Serializable {

    private Users currentUser;

    public void loginUser(Users user) {
        this.currentUser = user;
    }

    public Users getCurrentUser() {
        return currentUser;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public String getGhanaCardNumber() {
        return currentUser != null ? currentUser.getGhanaCardNumber() : null;
    }

    public String userRoleName() {
        if (currentUser != null && currentUser.getUserRole() != null) {
            return currentUser.getUserRole().getUserRolename();
        }
        return "Unknown";
    }

    public boolean userActive() {
        return currentUser != null && currentUser.getStatus() == UserStatus.ACTIVE;
    }

    public Set<String> fetchViewPermissions() {
        if (currentUser != null && currentUser.getUserRole() != null) {
            Set<ViewPermission> permissions = currentUser.getUserRole().getPermissions();
            Set<String> permissionsString = new HashSet<>();

            for (ViewPermission p : permissions) {
                permissionsString.add(p.getDisplayName());
            }
            return permissionsString;
        }
        return new HashSet<>();
    }

    public int getId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    //Check if the person has access to a page and deny access if entered manually 
    public boolean hasAccessTo(String pagePath) {
        if (currentUser == null || currentUser.getUserRole() == null) {
            return false;
        }

        String roleName = currentUser.getUserRole().getUserRolename();

        if ("SUPER ADMIN".equalsIgnoreCase(roleName)) {
            return true;
        }

        Set<ViewPermission> permissions = currentUser.getUserRole().getPermissions();

        return permissions.stream()
                .anyMatch(p -> p.getPagePath().equalsIgnoreCase(pagePath));
    }

    public boolean hasAnyPermission(String... permissionNames) {
        Set<String> userPermissions = fetchViewPermissions();
        for (String permission : permissionNames) {
            if (userPermissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermission(String permissionName) {
        return fetchViewPermissions().contains(permissionName);
    }
    
    public boolean checkAdmin() {
        return currentUser != null && currentUser.getUserType() == UserType.ADMIN;
    }

    public String getUserInitials() {
        if (currentUser == null || currentUser.getUsername() == null) {
            return "??";
        }

        String fullName = currentUser.getUsername().trim();
        String[] parts = fullName.split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return parts[0].substring(0, 1).toUpperCase()
                + parts[parts.length - 1].substring(0, 1).toUpperCase();
    }

    public void logout() throws IOException {
        currentUser = null;
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession(); // Ends user session
        ec.redirect(ec.getRequestContextPath() + "/login.xhtml");
    }
}
