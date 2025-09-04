/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.SystemUserRoles;
import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.service.UserRolesServices;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Named("roleManagementBean")
@ViewScoped
public class RoleManagementBean implements Serializable {

    @EJB
    private UserRolesServices userRoleService;

    @EJB
    private User_Service permissionService;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    private SystemUserRoles selectedRole = new SystemUserRoles();

    private List<SystemUserRoles> allRoles;

    private List<ViewPermission> allPermissions;
    private List<ViewPermission> selectedPermissions;
    private String searchQuery;
    private boolean editMode;

    @PostConstruct
    public void init() {
        loadRoles();
        allPermissions = permissionService.findAll();
    }

    public void loadRoles() {
        allRoles = userRoleService.getAllUserRoles();
    }

    public void openNewRoleDialog() {
        selectedRole = new SystemUserRoles();
        selectedPermissions = new ArrayList<>();
        editMode = false;
    }

    public void createRole() {
        System.out.println("=== CREATE ROLE METHOD CALLED ===");
        System.out.println("Selected role: " + selectedRole);
        System.out.println("Role name: " + (selectedRole != null ? selectedRole.getUserRolename() : "null"));
        System.out.println("Selected permissions: " + selectedPermissions);
        System.out.println("Selected permissions size: " + (selectedPermissions != null ? selectedPermissions.size() : "null"));
        if (userRoleService.findUserRoleByName(selectedRole.getUserRolename()) != null) {
            JSF.addWarningMessageWithSummary("NOTIFICATION", "System Role " + selectedRole.getUserRolename() + " already exists!");
            return;
        }
        if (selectedRole != null && selectedRole.getUserRolename() != null && !selectedRole.getUserRolename().trim().isEmpty()) {
            selectedRole.setPermissions(new HashSet<>(selectedPermissions));
            userRoleService.createUserRole(selectedRole);
            loadRoles();
            JSF.addSuccessMessageWithSummary("Success", "Role created Successfully");
        } else {
            JSF.addErrorMessage("Role Creation error - Missing role name");
        }

    }

    public void updateRole() {
        System.out.println("? updateRole() CALLED");

        if (selectedRole != null) {
            System.out.println("SelectedRole >>>>>>>>>>> " + selectedRole.getUserRolename());
            System.out.println(" selectedRole ID: " + selectedRole.getId());
            System.out.println("SelectedPermissions: " + selectedPermissions);

            selectedRole.setPermissions(new HashSet<>(selectedPermissions));
            System.out.println("SelectedPermissions: " + selectedPermissions);
            userRoleService.updateUserRole(selectedRole);
            loadRoles();
            JSF.addSuccessMessageWithSummary("Success", "Role edited Succesfully");
        } else {
            System.out.println(" selectedRole is NULL");

            JSF.addErrorMessage("Role Update error");
        }
    }

    public void deleteRole(SystemUserRoles role) {
        userRoleService.deleteUserRole(role);
        loadRoles();
    }

    public void editRole(SystemUserRoles role) {
        this.selectedRole = role;
        this.selectedPermissions = new ArrayList<>(role.getPermissions());
        editMode = true;
    }

    public void findUserRole2() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            allRoles = userRoleService.getAllUserRoles();
        } else {
            String lowerSearchQuery = searchQuery.toLowerCase();
            allRoles = userRoleService.getAllUserRoles().stream()
                    .filter(r -> r.getUserRolename().toLowerCase().contains(lowerSearchQuery))
                    .collect(Collectors.toList());
        }
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setCreateUserRole();
    }
}
