/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.EntrancesService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Cacheable;

/**
 *
 * @author PhilipManteAsare
 */
@Named("assignRoleBean")
@ViewScoped
@Cacheable(false)
public class AssignRoleBean implements Serializable {

    @Inject
    private EmployeeRole_Service employeeRole;

    @Inject
    private EntrancesService entrancesService;

    private List<EmployeeRole> allRoles;
    private List<Entrances> allEntrances;
    private String selectedEntranceId;
    private Set<Integer> selectedRolesIds;
    private List<EmployeeRole> assignedRoles;
    private String selectedEmployeeRoleId;
    
    @PostConstruct
    public void init() {
        allRoles = getAllRoles();
        allEntrances = getAllEntrances();
    }

    public List<EmployeeRole> getAllRoles() {
        return employeeRole.findAllEmployeeRoles();
    }

    public List<Entrances> getAllEntrances() {
        return entrancesService.findAllEntrances();
    }

    public void assignRolesToEntrance() {
        if (selectedEntranceId != null && selectedRolesIds != null && !selectedRolesIds.isEmpty()) {
            Entrances entrance = entrancesService.findEntranceById(selectedEntranceId);
            if (entrance == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Entrance not found"));
                return;
            }

            // Fetch new roles
            Set<EmployeeRole> newRoles = new HashSet<>(employeeRole.findEmployeeRolesByIds(selectedRolesIds));

            // Update inverse side (Entrances.allowedRoles)
            Set<EmployeeRole> existingRoles = entrance.getAllowedRoles();
            if (existingRoles == null) {
                existingRoles = new HashSet<>();
            }
            existingRoles.addAll(newRoles);
            entrance.setAllowedRoles(existingRoles);

            // Update owning side (EmployeeRole.accessibleEntrances)
            for (EmployeeRole role : newRoles) {
                Set<Entrances> accessibleEntrances = role.getAccessibleEntrances();
                if (accessibleEntrances == null) {
                    accessibleEntrances = new HashSet<>();
                }
                accessibleEntrances.add(entrance);
                role.setAccessibleEntrances(accessibleEntrances);
                employeeRole.save(role); // Persist the owning side
            }

            // Save the entrance (optional)
            entrancesService.save(entrance);
            loadAssignedRoles();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Roles assigned successfully!"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Select an entrance and at least one role"));
        }
    }


    
    public List<Entrances> searchEntrances(String query) {
        return allEntrances.stream()
                .filter(e -> e.getEntrance_Name().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public void loadAssignedRoles() {
        if (selectedEntranceId != null && !selectedEntranceId.trim().isEmpty()) {
            Entrances entrance = entrancesService.findEntranceById(selectedEntranceId);
            if (entrance != null) {
                assignedRoles = new ArrayList<>(entrance.getAllowedRoles());
            } else {
                assignedRoles = new ArrayList<>();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No roles assigned to this entrance"));
            }
        }
    }


    public void removeRoleFromEntrance(int roleId) {
        if (selectedEntranceId != null && !selectedEntranceId.trim().isEmpty()) {
            Entrances entrance = entrancesService.findEntranceById(selectedEntranceId);
            if (entrance != null) {
                // Find the role to remove
                EmployeeRole roleToRemove = entrance.getAllowedRoles()
                        .stream()
                        .filter(role -> role.getId() == roleId)
                        .findFirst()
                        .orElse(null);

                if (roleToRemove != null) {
                    // Update inverse side: Remove role from entrance
                    entrance.getAllowedRoles().remove(roleToRemove);

                    // Update owning side: Remove entrance from role
                    roleToRemove.getAccessibleEntrances().remove(entrance);

                    // Persist both entities
                    entrancesService.save(entrance);
                    employeeRole.save(roleToRemove);
                    
                     loadAssignedRoles();

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Role removed successfully!"));
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Role not found for this entrance"));
                }

                loadAssignedRoles(); 
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Entrance not found"));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No entrance selected"));
        }
    }
    
    // Method to check access for a role to an entrance
    public void checkAccess() {
        if (selectedEmployeeRoleId == null || selectedEmployeeRoleId.isEmpty()
                || selectedEntranceId == null || selectedEntranceId.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Please select both Role and Entrance"));
            return;
        }

        boolean hasAccess = entrancesService.canAccessEntrance(selectedEmployeeRoleId, selectedEntranceId);

        if (hasAccess) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Access Granted", "You have access to this entrance"));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Access Denied", "You do not have access to this entrance"));
        }
    }



    public List<EmployeeRole> getAssignedRoles() {
        return assignedRoles;
    }
    
    public String getSelectedEntranceId() {
        return selectedEntranceId;
    }

    public void setSelectedEntranceId(String selectedEntranceId) {
        this.selectedEntranceId = selectedEntranceId;
    }

    public Set<Integer> getSelectedRolesIds() {
        return selectedRolesIds;
    }

    public void setSelectedRolesIds(Set<Integer> selectedRolesIds) {
        this.selectedRolesIds = selectedRolesIds;
    }

    public String getSelectedEmployeeRoleId() {
        return selectedEmployeeRoleId;
    }

    public void setSelectedEmployeeRoleId(String selectedEmployeeRoleId) {
        this.selectedEmployeeRoleId = selectedEmployeeRoleId;
    }

}
