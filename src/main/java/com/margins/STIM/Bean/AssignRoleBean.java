/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.RoleTimeAccess;

import com.margins.STIM.entity.TimeAccessRule;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.TimeAccessRuleService;
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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
    private EmployeeRole_Service employeeRole;

    @Inject
    private EntrancesService entrancesService;

    @Inject
    private TimeAccessRuleService timeAccessRuleService;

    private String selectedEntranceId;
    private Set<Integer> selectedRolesIds;
    private List<EmployeeRole> assignedRoles;
    private String selectedEmployeeRoleId;
 

    @Getter
    @Setter
    private List<String> selectedEntranceIds;
    @Setter
    private List<EmployeeRole> allRoles;
    @Setter
    private List<Entrances> allEntrances;
    @Getter
    @Setter
    private TimeAccessRule newRule;
    @Getter
    @Setter
    private RoleTimeAccess dayRule;
    @Getter
    @Setter
    private List<String> newRuleDays;
    @Getter
    @Setter
    private Entrances selectedEntrance;
    @Getter
    @Setter
    private EmployeeRole currentRole;
    
    @Getter
    @Setter
    private List<String> selectedDays = new ArrayList<>();
    @Getter
    @Setter
    private Map<String, LocalTime> startTimes = new HashMap<>();
    @Getter
    @Setter
    private Map<String, LocalTime> endTimes = new HashMap<>();

    
    @PostConstruct
    public void init() {
        allRoles = getAllRoles();
        allEntrances = getAllEntrances();
        assignedRoles = new ArrayList<>();
        selectedRolesIds = new HashSet<>();
        newRule = new TimeAccessRule();
        newRuleDays = new ArrayList<>();
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
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
            if (selectedEntrance != null) {
                assignedRoles = new ArrayList<>(selectedEntrance.getAllowedRoles());
            } else {
                assignedRoles = new ArrayList<>();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No roles assigned to this entrance"));
            }
        } else {
            assignedRoles = new ArrayList<>();
        }
    }

    public void removeRoleFromEntrance(int roleId) {
        if (selectedEntranceId == null || selectedEntranceId.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No entrance selected"));
            return;
        }

        Entrances entrance = entrancesService.findEntranceById(selectedEntranceId);
        if (entrance == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Entrance not found"));
            return;
        }

        // Find the role to remove
        EmployeeRole roleToRemove = entrance.getAllowedRoles()
                .stream()
                .filter(r -> r.getId() == roleId)
                .findFirst()
                .orElse(null);

        if (roleToRemove == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Role not found for this entrance"));
            return;
        }

        try {
            // 1) Delete all time‐access rules for this role+entrance
            timeAccessRuleService.deleteTimeRulesByRoleAndEntrance(roleId, selectedEntranceId);

            // 2) Remove the bidirectional relationship
            entrance.getAllowedRoles().remove(roleToRemove);
            roleToRemove.getAccessibleEntrances().remove(entrance);

            // 3) Persist both sides
            entrancesService.save(entrance);
            employeeRole.save(roleToRemove);

            // 4) Refresh UI model
            loadAssignedRoles();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Role and its time rules removed!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Could not remove role: " + e.getMessage()));
        }
    }

    public void prepareTimeRule(EmployeeRole role) {
        // Reset the rule
        newRule = new TimeAccessRule();
        newRule.setRole(role);
        this.currentRole = role;

        // Reset days
        newRuleDays = new ArrayList<>();

        // Check for existing rule for this role and entrance
        List<TimeAccessRule> existingRules = timeAccessRuleService.getTimeRulesByRole(role.getId());

        // Find rule for the selected entrance
        existingRules.stream()
                .filter(r -> r.getEntrance().getEntrance_Device_ID().equals(selectedEntranceId))
                .findFirst()
                .ifPresent(existingRule -> {
                    // Populate existing rule
                    newRule = existingRule;

                    // Populate days
                    if (existingRule.getDaysOfWeek() != null && !existingRule.getDaysOfWeek().isEmpty()) {
                        newRuleDays = new ArrayList<>(Arrays.asList(existingRule.getDaysOfWeek().split(",")));
                    }
                });

        // Ensure entrance is set
        if (selectedEntrance == null && selectedEntranceId != null) {
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
        }
        newRule.setEntrance(selectedEntrance);
    }
    
    public void prepareDayTimeRules(EmployeeRole role){
        dayRule = new RoleTimeAccess();
        dayRule.setEmployeeRole(role);
        this.currentRole = role;
        
        if (selectedEntrance == null && selectedEntranceId != null) {
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
        }
        dayRule.setEntrances(selectedEntrance);
    }
    
    public void loadDayTimeInputs() {
        if (selectedDays != null) {
            for (String day : selectedDays) {
                startTimes.putIfAbsent(day, null);
                endTimes.putIfAbsent(day, null);
            }
            
            System.out.println("Selected Days>>>>>>"+ selectedDays);
        }
    }
    
    public void saveDayTimeRules() {
        if (currentRole != null && selectedEntrance != null && selectedDays != null) {
            timeAccessRuleService.saveOrUpdateRoleTimeAccess(currentRole, selectedEntrance, startTimes, endTimes, selectedDays);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved", "Day time access rules saved successfully."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Missing role, entrance, or selected days."));
        }
    }
    
    public void saveTimeRule() {
        // Validate inputs
        if (validateTimeRule()) {
            try {

                // Set days of week
                newRule.setDaysOfWeek(String.join(",", newRuleDays));

                // Save the rule
                timeAccessRuleService.saveTimeRule(newRule);

                // Show success message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success",
                                "Time rule saved successfully!"));

                // Optional: Load assigned roles or perform any post-save actions
                loadAssignedRoles();
            } catch (Exception e) {
                // Handle any exceptions during save
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Error",
                                "Failed to save time rule: " + e.getMessage()));
            }
        }
    }
    

    private boolean validateTimeRule() {
        FacesContext context = FacesContext.getCurrentInstance();
        boolean isValid = true;

        // Validate start time
        if (newRule.getStartTime() == null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Validation Error",
                            "Start time is required"));
            isValid = false;
        }

        // Validate end time
        if (newRule.getEndTime() == null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Validation Error",
                            "End time is required"));
            isValid = false;
        }

        // Proceed only if both times are set
        if (newRule.getStartTime() != null && newRule.getEndTime() != null) {
            Date startDate = newRule.getStartTime();
            Date endDate = newRule.getEndTime();

            LocalTime startTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime endTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            if (endTime.equals(startTime)) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Validation Error",
                                "Start and end time cannot be the same"));
                isValid = false;
            }

            LocalDate today = LocalDate.now();
            LocalDateTime startDateTime = LocalDateTime.of(today, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(today, endTime);

            // Handle overnight time span
            if (endTime.isBefore(startTime)) {
                endDateTime = endDateTime.plusDays(1);
            }

            Duration duration = Duration.between(startDateTime, endDateTime);
            if (duration.isNegative() || duration.isZero()) {
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Validation Error",
                                "End time must be after start time"));
                isValid = false;
            }
        }

        // Validate days selection (outside of time validation block)
        if (newRuleDays == null || newRuleDays.isEmpty()) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Validation Warning",
                            "Please Select Days. The rule will not be applied."));
            isValid = false;
        }

        return isValid;
    }

    public String getTimeRulesForRoleAndEntrance(int roleId) {
        List<TimeAccessRule> rules = timeAccessRuleService.getTimeRulesByRole(roleId);

        return rules.stream()
                .filter(r -> r.getEntrance().getEntrance_Device_ID().equals(selectedEntranceId))
                .map(r -> formatTimeRange(r.getStartTime(), r.getEndTime(), r.getDaysOfWeek()))
                .findFirst()
                .orElse("No time rules");
    }

    public String formatTimeRange(Date start, Date end, String daysCsv) {
        if (start == null || end == null) {
            return "-";
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String formattedStart = timeFormat.format(start);
        String formattedEnd = timeFormat.format(end);

        String days = formatDays(daysCsv);
        return formattedStart + " - " + formattedEnd + " (" + days + ")";
    }

    public String formatDays(String daysCsv) {
        if (daysCsv == null || daysCsv.trim().isEmpty()) {
            return "-";
        }

        Map<String, String> dayMap = Map.of(
                "MON", "Monday", "TUE", "Tuesday", "WED", "Wednesday",
                "THU", "Thursday", "FRI", "Friday", "SAT", "Saturday", "SUN", "Sunday"
        );

        return Arrays.stream(daysCsv.split(","))
                .map(String::trim)
                .map(code -> dayMap.getOrDefault(code, code))
                .collect(Collectors.joining(", "));
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

//    public boolean isRoleFirstMode() {
//        return roleFirstMode;
//    }
//
//    public void toggleMode() {
//        roleFirstMode = !roleFirstMode;
//
//        String message = roleFirstMode
//                ? "Switched to Role-First Mode"
//                : "Switched to Entrance-First Mode";
//
//        FacesContext.getCurrentInstance().addMessage(null,
//                new FacesMessage(FacesMessage.SEVERITY_INFO, "Mode Changed", message));
//    }
//
//    public void assignEntrancesToRole() {
//        if (selectedRoleId != null && selectedEntranceIds != null && !selectedEntranceIds.isEmpty()) {
//            EmployeeRole role = employeeRole.findEmployeeRoleById(selectedRoleId);
//            if (role == null) {
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Role not found"));
//                return;
//            }
//
//            List<Entrances> entrancesToAdd = entrancesService.findEntranceByIds(selectedEntranceIds);
//            if (entrancesToAdd == null || entrancesToAdd.isEmpty()) {
//                FacesContext.getCurrentInstance().addMessage(null,
//                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No valid entrances found"));
//                return;
//            }
//
//            // Ensure role's accessible entrances is initialized
//            Set<Entrances> currentEntrances = role.getAccessibleEntrances();
//            if (currentEntrances == null) {
//                currentEntrances = new HashSet<>();
//            }
//
//            // Add new entrances
//            currentEntrances.addAll(entrancesToAdd);
//            role.setAccessibleEntrances(currentEntrances);
//
//            // Update the inverse side: Entrances.allowedRoles
//            for (Entrances entrance : entrancesToAdd) {
//                if (entrance.getAllowedRoles() == null) {
//                    entrance.setAllowedRoles(new HashSet<>());
//                }
//                entrance.getAllowedRoles().add(role);
//                entrancesService.save(entrance); // Optional, depending on cascade
//            }
//
//            employeeRole.save(role); // Save role as it’s the owning side
//
//            FacesContext.getCurrentInstance().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrances assigned to role successfully!"));
//        } else {
//            FacesContext.getCurrentInstance().addMessage(null,
//                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Select a role and at least one entrance"));
//        }
//    }

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
