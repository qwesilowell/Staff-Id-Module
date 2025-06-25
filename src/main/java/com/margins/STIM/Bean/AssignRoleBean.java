/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;

import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.TimeAccessRuleService;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.JSF;
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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("assignRoleBean")
@ViewScoped
@Cacheable(false)
public class AssignRoleBean implements Serializable {

    @Inject
    private EmployeeRole_Service employeeRoleService;

    @Inject
    private EntrancesService entrancesService;

    @Inject
    private TimeAccessRuleService timeAccessRuleService;
    
    @Inject
    private BreadcrumbBean breadcrumbBean;

    private String selectedEntranceId;
    private Set<Integer> selectedRolesIds;
    private List<EmployeeRole> assignedRoles;
    private EmployeeRole selectedEmployeeRoleId;

    @Getter
    @Setter
    private List<String> selectedEntranceIds;
    @Setter
    private List<EmployeeRole> allRoles;
    @Setter
    private List<Entrances> allEntrances;
    @Getter
    @Setter
    private RoleTimeAccess dayRule;
    @Getter
    @Setter
    private Entrances selectedEntrance;
    @Getter
    @Setter
    private EmployeeRole currentRole;
    @Getter
    private boolean timeRuleValid;
    @Getter
    @Setter
    private List<String> selectedDays = new ArrayList<>();
    @Getter
    @Setter
    private Map<String, LocalTime> startTimes = new HashMap<>();
    @Getter
    @Setter
    private Map<String, LocalTime> endTimes = new HashMap<>();
    @Getter
    @Setter
    private Map<Integer, Boolean> roleHasTimeRules = new HashMap<>();
    @Getter
    @Setter
    private List<EmployeeRole> availableRolesForSelection = new ArrayList<>();
    @Getter
    @Setter
    private Map<Integer, String> roleStatusMessages = new HashMap<>();

    @PostConstruct
    public void init() {
        allRoles = getAllRoles();
        allEntrances = getAllEntrances();
        assignedRoles = new ArrayList<>();
        selectedRolesIds = new HashSet<>();
    }

    public List<EmployeeRole> getAllRoles() {
        return employeeRoleService.findAllEmployeeRoles();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setAssignRolesToEntrancesBreadcrumb();
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

            // Update inverse side (Entrances.allowedRoles)
            Set<EmployeeRole> existingRoles = entrance.getAllowedRoles();
            if (existingRoles == null) {
                existingRoles = new HashSet<>();
            }

            List<EmployeeRole> newRoles = employeeRoleService.findEmployeeRolesByIds(selectedRolesIds);

            // Fetch new roles
            for (EmployeeRole role : newRoles) {
                // Only add if not already associated
                if (!existingRoles.contains(role)) {
                    existingRoles.add(role);
                    entrance.setAllowedRoles(existingRoles); // Optional but good practice

                    Set<Entrances> accessibleEntrances = role.getAccessibleEntrances();
                    if (accessibleEntrances == null) {
                        accessibleEntrances = new HashSet<>();
                    }

                    if (!accessibleEntrances.contains(entrance)) {
                        accessibleEntrances.add(entrance);
                        role.setAccessibleEntrances(accessibleEntrances);
                        employeeRoleService.save(role); // Save owning side
                    }
                }
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
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
            if (selectedEntrance != null) {
                assignedRoles = new ArrayList<>(selectedEntrance.getAllowedRoles());
                
                availableRolesForSelection = allRoles.stream()
                        .filter(role -> assignedRoles.stream().noneMatch(ar -> ar.getId()==(role.getId())))
                        .collect(Collectors.toList());
                checkTimeRulesForAssignedRoles();
            } else {
                assignedRoles = new ArrayList<>();
                roleHasTimeRules.clear();
                roleStatusMessages.clear();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No roles assigned to this entrance"));
            }
        } else {
            assignedRoles = new ArrayList<>();
            roleHasTimeRules.clear();
            roleStatusMessages.clear();
        }
    }

    private void checkTimeRulesForAssignedRoles() {
        roleHasTimeRules.clear();
        roleStatusMessages.clear();

        if (selectedEntrance != null && assignedRoles != null) {
            for (EmployeeRole role : assignedRoles) {
                List<RoleTimeAccess> existingRules = timeAccessRuleService.findByRoleAndEntrance(role, selectedEntrance);

                if (existingRules != null && !existingRules.isEmpty()) {
                    roleHasTimeRules.put(role.getId(), true);
                    roleStatusMessages.put(role.getId(), "Role time access created - " + existingRules.size() + " rule(s) found");
                } else {
                    roleHasTimeRules.put(role.getId(), false);
                    roleStatusMessages.put(role.getId(), "No time access configured");
                }
            }
        }
    }

    public boolean hasTimeRules(EmployeeRole role) {
        return roleHasTimeRules.getOrDefault(role.getId(), false);
    }

// a method to refresh time rule status (useful after saving rules)
    public void refreshTimeRuleStatus() {
        if (selectedEntrance != null && assignedRoles != null) {
            checkTimeRulesForAssignedRoles();
        }
    }

    public void prepareDayTimeRules(EmployeeRole role) {
        timeRuleValid = false;
        dayRule = new RoleTimeAccess();
        dayRule.setEmployeeRole(role);
        this.currentRole = role;

        if (selectedEntrance == null && selectedEntranceId != null) {
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
        }
        dayRule.setEntrances(selectedEntrance);

        selectedDays = new ArrayList<>();
        startTimes = new HashMap<>();
        endTimes = new HashMap<>();

        List<RoleTimeAccess> existingRules = timeAccessRuleService.findByRoleAndEntrance(role, selectedEntrance);

        // Populate selectedDays from existingRules
        for (RoleTimeAccess rule : existingRules) {
            String day = rule.getDayOfWeek().name();
            if (!selectedDays.contains(day)) {
                selectedDays.add(day);
            }
        }

        // Now load times for these days into startTimes and endTimes maps
        for (RoleTimeAccess rule : existingRules) {
            String day = rule.getDayOfWeek().name();
            if (selectedDays.contains(day)) {
                LocalTime start = DateFormatter.toLocalTime(rule.getStartTime());
                LocalTime end = DateFormatter.toLocalTime(rule.getEndTime());

                startTimes.put(day, start);
                endTimes.put(day, end);
            }
        }
    }

    public void loadDayTimeInputs() {
        if (selectedDays != null) {
            for (String day : selectedDays) {
                startTimes.putIfAbsent(day, null);
                endTimes.putIfAbsent(day, null);
            }

            System.out.println("Selected Days>>>>>>" + selectedDays);
        }
    }

    public void saveDayTimeRules() {
        if (validateTimeRule()) {
            timeAccessRuleService.saveOrUpdateRoleTimeAccess(currentRole, selectedEntrance, startTimes, endTimes, selectedDays);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved", "Day time access rules saved successfully."));
            timeRuleValid = true;
            refreshTimeRuleStatus();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed To Save."));
            timeRuleValid = false;
        }
    }

    public boolean validateTimeRule() {
        boolean isValid = true;

        if (currentRole == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Role not Set", "Refresh Page"));
            return false;
        }
        // Validate start time
        if (selectedDays == null || selectedDays.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation Error", "Select at least one day"));
            isValid = false;
        }

        for (String day : selectedDays) {
            LocalTime startTime = startTimes.get(day);
            LocalTime endTime = endTimes.get(day);

            if (startTime == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation Error", "Start time is required for " + day));
                isValid = false;
            }

            if (endTime == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation Error", "End time is required for " + day));
                isValid = false;
            }

            if (startTime != null && endTime != null) {
                if (endTime.equals(startTime)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Validation Error", "Start and end time cannot be the same for " + day));
                    isValid = false;
                } else if (endTime.isBefore(startTime)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Validation Error", "End time must be after start time for " + day));
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    public void removeRole() {
        if (selectedEntranceId != null && currentRole != null) {
            employeeRoleService.removeRoleAccessWithTimeRules(currentRole.getId(), selectedEntranceId);
            Entrances ent = entrancesService.findEntranceById(selectedEntranceId);

            loadAssignedRoles();

            JSF.addSuccessMessageWithSummary("Success", currentRole.getRoleName() + " and time access removed for " + ent.getEntrance_Name());
            currentRole = null;
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
}
