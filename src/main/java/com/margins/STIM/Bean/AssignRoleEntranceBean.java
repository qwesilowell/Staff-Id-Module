/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.TimeAccessRuleService;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("assignRoleEntranceBean")
@ViewScoped
@Getter
@Setter
public class AssignRoleEntranceBean implements Serializable {

    @Inject
    private EmployeeRole_Service employeeRoleService;
    @Inject
    private EntrancesService entranceService;
    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;
    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private TimeAccessRuleService timeAccessRuleService;

    private List<EmployeeRole> allRoles;
    private EmployeeRole selectedRole;
    private Set<Entrances> selectedEntrances;
    private Set<Entrances> availablEntrancesForSelection = new HashSet<>();
    private Set<Entrances> assignedEntrances = new HashSet<>();
    private List<Entrances> allEntrances;
    private Map<Integer, Boolean> entranceHasTimeRules = new HashMap<>();
    private Entrances selectedEntrance;
    private Users currentUser;
    private boolean timeRuleValid;
    private RoleTimeAccess dayRule;
    private List<String> selectedDays = new ArrayList<>();
    private Map<String, LocalTime> startTimes = new HashMap<>();
    private Map<String, LocalTime> endTimes = new HashMap<>();
    private LocalTime defaultStartTime;
    private LocalTime defaultEndTime;

    @PostConstruct
    public void init() {
        allRoles = getAllRoles();
        allEntrances = entranceService.findAllEntrances();
        currentUser = userSession.getCurrentUser();
        assignedEntrances = new HashSet<>();
        selectedEntrances = new HashSet<>();

    }

    public List<EmployeeRole> getAllRoles() {
        return employeeRoleService.findAllEmployeeRoles();
    }

    public List<EmployeeRole> searchRole(String query) {
        return allRoles.stream()
                .filter(r -> r.getRoleName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void loadAssignedEntrances() {
        if (selectedRole != null) {
            assignedEntrances = new HashSet<>(selectedRole.getAccessibleEntrances());

            availablEntrancesForSelection = allEntrances.stream()
                    .filter(entrance -> assignedEntrances.stream().noneMatch(e -> e.getId() == (entrance.getId())))
                    .collect(Collectors.toSet());
            refreshTimeRuleStatus();
        }
    }

    public void assignEntrancesToRole() {
        if (selectedEntrances != null && !selectedEntrances.isEmpty()) {
            EmployeeRole role = selectedRole;
            if (role == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Role not found"));
                return;
            }

            // Always non-null because initialized in entity
            Set<Entrances> accessibleEntrances = role.getAccessibleEntrances();

            for (Entrances entrance : selectedEntrances) {
                if (!accessibleEntrances.contains(entrance)) {
                    // update owning side
                    accessibleEntrances.add(entrance);

                    // update inverse side for in-memory sync
                    entrance.getAllowedRoles().add(role);
                }
            }

            // Save the owning side
            role.setAccessibleEntrances(accessibleEntrances);
            employeeRoleService.save(role);

            String detail = "Assigned Entrances to Role " + role.getRoleName() + ".";
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Entrances To Role",
                    ActionResult.SUCCESS, detail, currentUser);

            loadAssignedEntrances(); // refresh UI list

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrances assigned successfully!"));

        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Select least one entrance"));
            String detail = "Failed to Assign Entrances.";
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Entrances To Role",
                    ActionResult.FAILED, detail, currentUser);
        }
    }

    public boolean hasTimeRules(Entrances entrance) {
        return entranceHasTimeRules.getOrDefault(entrance.getId(), false);
    }

    public void refreshTimeRuleStatus() {
        if (selectedRole != null && assignedEntrances != null) {
            checkTimeRulesForAssignedEntrances();
        }
    }

    private void checkTimeRulesForAssignedEntrances() {
        entranceHasTimeRules.clear();

        if (assignedEntrances != null && selectedRole != null) {
            for (Entrances ent : assignedEntrances) {
                List<RoleTimeAccess> existingRules = timeAccessRuleService.findByRoleAndEntrance(selectedRole, ent);

                if (existingRules != null && !existingRules.isEmpty()) {
                    entranceHasTimeRules.put(ent.getId(), true);
                } else {
                    entranceHasTimeRules.put(ent.getId(), false);
                }
            }
        }
    }

    public void removeEntrance() {
        if (selectedEntrance != null && selectedRole != null) {
            employeeRoleService.removeRoleAccessWithTimeRules(selectedRole, selectedEntrance);
            Entrances ent = selectedEntrance;

            String detail = "Removed Role " + selectedRole.getRoleName() + " from accesing" + selectedEntrance.getEntranceName() + ".";
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Roles To Entrance", ActionResult.SUCCESS, detail, currentUser);

            loadAssignedEntrances();

            JSF.addSuccessMessageWithSummary("Success", selectedRole.getRoleName() + " and time access removed for " + ent.getEntranceName());
            selectedEntrance = null;
        }
    }

    public void prepareDayTimeRules(Entrances entrance) {
        timeRuleValid = false;
        dayRule = new RoleTimeAccess();
        selectedEntrance = entrance;
        dayRule.setEntrances(entrance);

        dayRule.setEmployeeRole(selectedRole);

        selectedDays = new ArrayList<>();
        startTimes = new HashMap<>();
        endTimes = new HashMap<>();

        List<RoleTimeAccess> existingRules = timeAccessRuleService.findByRoleAndEntrance(selectedRole, selectedEntrance);

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
    // Method to fill default times to all selected days

    public void fillDefaultTimesToSelectedDays() {
        if (defaultStartTime != null && defaultEndTime != null && !selectedDays.isEmpty()) {
            for (String day : selectedDays) {
                startTimes.put(day, defaultStartTime);
                endTimes.put(day, defaultEndTime);
            }

            // Optional: Show success message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success",
                            "Default times applied to all selected days!"));
        } else {
            // Show validation message
            String message = "";
            if (selectedDays.isEmpty()) {
                message = "Please select at least one day first.";
            } else if (defaultStartTime == null || defaultEndTime == null) {
                message = "Please set both start and end default times.";
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Warning", message));
        }
    }

    public void selectAllDays() {
        selectedDays = new ArrayList<>(Arrays.asList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY",
                "FRIDAY", "SATURDAY", "SUNDAY"
        ));
        loadDayTimeInputs();
    }

    public void selectWeekdays() {
        selectedDays = new ArrayList<>(Arrays.asList(
                "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"
        ));
        loadDayTimeInputs();
    }

    public void selectWeekends() {
        selectedDays = new ArrayList<>(Arrays.asList(
                "SATURDAY", "SUNDAY"
        ));
        loadDayTimeInputs();
    }

    public void clearAllDays() {
        selectedDays = new ArrayList<>();
        // Clear the time inputs as well
        startTimes.clear();
        endTimes.clear();
    }

    public void selectWeekdaysWithBusinessHours() {
        selectWeekdays();
        setBusinessHoursAsDefault();
        fillDefaultTimesToSelectedDays();
    }

    public void selectWeekendsWithFlexibleHours() {
        selectWeekends();
        setFlexibleHoursAsDefault();
        fillDefaultTimesToSelectedDays();
    }

// Helper methods for common time presets
    private void setBusinessHoursAsDefault() {
        defaultStartTime = LocalTime.of(7, 0);   // 7:00 AM
        defaultEndTime = LocalTime.of(17, 0);    // 5:00 PM
    }

    private void setFlexibleHoursAsDefault() {
        defaultStartTime = LocalTime.of(5, 0);  // 5:00 AM
        defaultEndTime = LocalTime.of(22, 0);    // 10:00 PM
    }

    public void deleteDayTimeRule(String day) {
        try {
            if (selectedDays.contains(day)) {
                // Remove from selected days list (UI state)
                selectedDays.remove(day);

                // Remove associated times from maps (UI state)
                startTimes.remove(day);
                endTimes.remove(day);

                // dayRule object available, delete from database
                if (dayRule != null && dayRule.getEmployeeRole() != null && dayRule.getEntrances() != null) {
                    timeAccessRuleService.deleteRoleTimeAccess(
                            dayRule.getEmployeeRole(),
                            dayRule.getEntrances(),
                            day
                    );
                    String detail = "Removed " + day + " access from " + dayRule.getEntrances().getEntranceName() + " for role: " + dayRule.getEmployeeRole();
                    System.out.println("detail>>>>>>>>>>> " + detail);
                    auditLogService.logActivity(AuditActionType.CREATE, "Assign Roles To Entrance", ActionResult.SUCCESS, detail, currentUser);

                    JSF.addSuccessMessageWithSummary("Success", day + " removed.");
                }
            }
        } catch (Exception e) {
            // Re-add to UI state if something went wrong
            if (!selectedDays.contains(day)) {
                selectedDays.add(day);
            }

            FacesContext.getCurrentInstance().addMessage("sidebarForm:sidebarMessages",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error",
                            "Failed to delete " + day + ": " + e.getMessage()));

            e.printStackTrace(); // For debugging
        }
    }

    public void saveDayTimeRules() {
        if (validateTimeRule()) {
            timeAccessRuleService.saveOrUpdateRoleTimeAccess(selectedRole, selectedEntrance, startTimes, endTimes, selectedDays);

            String details = "Created a day and time access for " + selectedRole.getRoleName() + " at " + selectedEntrance.getEntranceName() + ".";
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Roles to Entrance", ActionResult.SUCCESS, details, currentUser);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved", "Day time access rules saved successfully."));
            timeRuleValid = true;
            refreshTimeRuleStatus();
        } else {
            String details = "Failed to create a day and time access for " + selectedRole.getRoleName() + " at " + selectedEntrance.getEntranceName() + ".";
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Roles to Entrance", ActionResult.FAILED, details, currentUser);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed To Save."));
            timeRuleValid = false;
        }
    }

    public boolean validateTimeRule() {
        boolean isValid = true;

        if (selectedRole == null) {
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
    
    public void setupBreadcrumb(){
    breadcrumbBean.setAssignEntrancesToRoles(); 
    }
}
