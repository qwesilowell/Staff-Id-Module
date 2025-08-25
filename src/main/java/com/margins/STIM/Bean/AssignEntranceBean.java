package com.margins.STIM.Bean;

import com.margins.STIM.entity.CustomTimeAccess;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.TimeAccessRuleService;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityNotFoundException;
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
import org.primefaces.event.SelectEvent;

@Getter
@Setter
@Named("assignEntranceBean")
@SessionScoped
public class AssignEntranceBean implements Serializable {

    @EJB
    private Employee_Service employeeService;

    @EJB
    private EntrancesService entrancesService;

    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;

    @Inject
    private TimeAccessRuleService timeAccessRuleService;

    private String searchQuery;
    private List<Employee> employees;
    private Employee selectedEmployee;
    private List<Entrances> availableEntrances;
    private List<Entrances> selectedEntrances;
    private List<Entrances> assignedCustomEntrances;
    private CustomTimeAccess customRule;
    private List<Entrances> assignedRoleEntrances;

    private Entrances selectedEntrance;

    private List<String> selectedDays = new ArrayList<>();

    private Map<String, LocalTime> startTimes = new HashMap<>();

    private Map<String, LocalTime> endTimes = new HashMap<>();
    
    private LocalTime defaultStartTime;
    
    private LocalTime defaultEndTime;

    @PostConstruct
    public void init() {
        try {
            employees = getAllEmployees();
            availableEntrances = getAllEntrances();
            selectedEntrances = new ArrayList<>();
            assignedCustomEntrances = new ArrayList<>();
            assignedRoleEntrances = new ArrayList<>();
            searchQuery = "";
        } catch (Exception e) {
            logError("Error initializing data", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to initialize data.");
        }
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setAssignCustomEntranceBreadcrumb();
    }

    public List<Entrances> getAllEntrances() {
        return entrancesService.findAllEntrances(); // Fetch from DB once
    }

    public List<Employee> getAllEmployees() {
        return employeeService.findAllEmployees(); // Fetch from DB once
    }

    public void searchEmployees() {
        try {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                employees = employeeService.findAllEmployees();
            } else {
                String query = searchQuery.toLowerCase().trim();
                employees = employeeService.findAllEmployees().stream()
                        .filter(e -> e.getFullName().toLowerCase().contains(query)
                        || e.getGhanaCardNumber().toLowerCase().contains(query))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logError("Error searching employees", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to search employees.");
        }
    }

    public void openAssignPopup(Employee employee) {
        if (employee == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "No employee selected!");
            return;
        }
        try {
            this.selectedEmployee = employeeService.findEmployeeByGhanaCard(employee.getGhanaCardNumber());
            if (this.selectedEmployee == null) {
                showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Employee not found!");
                return;
            }
            this.availableEntrances = getAllEntrances();
            List<Entrances> customEntrances = selectedEmployee.getCustomEntrances() != null
                    ? selectedEmployee.getCustomEntrances() : new ArrayList<>();
            this.selectedEntrances = new ArrayList<>(customEntrances);
            this.assignedCustomEntrances = new ArrayList<>(customEntrances);
            this.assignedRoleEntrances = selectedEmployee.getRole() != null && selectedEmployee.getRole().getAccessibleEntrances() != null
                    ? new ArrayList<>(selectedEmployee.getRole().getAccessibleEntrances())
                    : new ArrayList<>();
        } catch (Exception e) {
            logError("Error opening assign popup", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to load employee entrances: " + e.getMessage());
        }
    }

    public List<Entrances> getFilteredAvailableEntrances() {
        Set<String> roleEntranceIds = new HashSet<>();
        if (assignedRoleEntrances != null) {
            assignedRoleEntrances.forEach(e -> roleEntranceIds.add(e.getEntranceDeviceId()));
        }
        if (assignedCustomEntrances != null) {
            assignedCustomEntrances.forEach(e -> roleEntranceIds.add(e.getEntranceDeviceId()));
        }
        return availableEntrances.stream()
                .filter(e -> !roleEntranceIds.contains(e.getEntranceDeviceId()))
                .collect(Collectors.toList());
    }

    public void saveCustomEntrances() {
        if (selectedEmployee == null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "No employee selected!");
            return;
        }
        try {
            System.out.println("Saving custom entrances for employee: " + selectedEmployee.getGhanaCardNumber());
            System.out.println("Selected entrances: " + selectedEntrances.stream()
                    .map(e -> e.getEntranceDeviceId() + ":" + e.getEntranceName())
                    .collect(Collectors.joining(", ")));

            // Combine existing assigned custom entrances with newly selected ones
            Set<String> existingIds = assignedCustomEntrances.stream()
                    .map(Entrances::getEntranceDeviceId)
                    .collect(Collectors.toSet());

            List<Entrances> allCustomEntrances = new ArrayList<>(assignedCustomEntrances);

            // Add only new selections that aren't already assigned
            for (Entrances selected : selectedEntrances) {
                if (!existingIds.contains(selected.getEntranceDeviceId())) {
                    allCustomEntrances.add(selected);
                }
            }

            // Update in database using service method
            Employee updatedEmployee = employeeService.updateEmployeeEntrances(selectedEmployee, allCustomEntrances);

            // Update local references
            selectedEmployee = updatedEmployee;
            assignedCustomEntrances = new ArrayList<>(allCustomEntrances);

            // Refresh available entrances
            availableEntrances = getAllEntrances();

            String detail = "Assigned custom entrance(s) to " + selectedEmployee.getFullName() + ".";
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Custom Entrance", ActionResult.SUCCESS, detail, userSession.getCurrentUser());

            // Refresh employees list
            employees = employeeService.findAllEmployees();

            showMessage(FacesMessage.SEVERITY_INFO, "Success", "Custom entrances updated for " + selectedEmployee.getFullName());

            // Clear selection state (but keep selectedEmployee for the dialog)
            selectedEntrances.clear();

        } catch (EntityNotFoundException e) {
            String errorDetail = "Failed to assign custom entrance(s) to " + selectedEmployee.getFullName() + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Custom Entrance", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
            logError("Employee not found", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Employee not found: " + selectedEmployee.getGhanaCardNumber());

        } catch (Exception e) {
            String errorDetail = "Failed to assign custom entrance(s) to " + selectedEmployee.getFullName() + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.CREATE, "Assign Custom Entrance", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
            logError("Error saving custom entrances", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save custom entrances: " + e.getMessage());
        }
    }

    public void removeCustomEntrance(Entrances entrance) {
        if (selectedEmployee == null || entrance == null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "No employee selected!");
            return;
        }
        try {
            System.out.println("Removing entrance " + entrance.getEntranceDeviceId() + " for employee: " + selectedEmployee.getGhanaCardNumber());

            // Remove entrance from the lists
            selectedEntrances.removeIf(e -> e.getEntranceDeviceId().equals(entrance.getEntranceDeviceId()));
            assignedCustomEntrances.removeIf(e -> e.getEntranceDeviceId().equals(entrance.getEntranceDeviceId()));

            // Update in database
            Employee updatedEmployee = employeeService.updateEmployeeEntrances(selectedEmployee, assignedCustomEntrances);

            employees = employeeService.findAllEmployees();

            // Update your local reference with the returned employee (important!)
            selectedEmployee = updatedEmployee;

            // Refresh available entrances
            availableEntrances = getAllEntrances();

            String successDetail = "Removed custom entrance (" + entrance.getEntranceName() + ") for " + selectedEmployee.getFullName();
            auditLogService.logActivity(AuditActionType.DELETE, "Remove Custom Entrance", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());

            // Change to INFO for success messages
            showMessage(FacesMessage.SEVERITY_INFO, "Success", "Removed custom entrance " + entrance.getEntranceName() + " for " + selectedEmployee.getFullName());

        } catch (Exception e) {
            String errorDetail = "Failed to remove entrance " + entrance.getEntranceName() + " for " + selectedEmployee.getFullName() + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.DELETE, "Remove Custom Entrance", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
            logError("Error removing custom entrance", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to remove entrance: " + e.getMessage());
        }
    }

    public void cancel() {
        selectedEmployee = null;
        selectedEntrances.clear();
        assignedCustomEntrances.clear();
        assignedRoleEntrances.clear();
    }

    public void viewEmployee(Employee employee) {
        selectedEmployee = employeeService.findEmployeeById(employee.getId());
        selectedEntrance = null;
        selectedDays = new ArrayList<>();
        startTimes = new HashMap<>();
        endTimes = new HashMap<>();
    }

    public List<Entrances> employeesEntrances(String query) {
        Set<Entrances> combinedEntrances = new HashSet<>();

        if (selectedEmployee != null && selectedEmployee.getCustomEntrances() != null) {
            combinedEntrances.addAll(selectedEmployee.getCustomEntrances());
        }

        if (selectedEmployee != null && selectedEmployee.getRole() != null && selectedEmployee.getRole().getAccessibleEntrances() != null) {
            combinedEntrances.addAll(selectedEmployee.getRole().getAccessibleEntrances());
        }
        return combinedEntrances.stream()
                .filter(e -> e.getEntranceDeviceId() != null)
                .collect(Collectors.toList());

    }

    public void prepareCustomTimeRules(SelectEvent<Entrances> event) {
        Entrances entrance = event.getObject();
        this.selectedEntrance = entrance;

        if (selectedEmployee != null) {
            customRule = new CustomTimeAccess();
            customRule.setEmployee(selectedEmployee);
            customRule.setEntrances(selectedEntrance);

            List<CustomTimeAccess> existingRules = timeAccessRuleService.findByEmployeeAndEntrance(selectedEmployee, selectedEntrance);

            // Clear current selectedDays first
            selectedDays = new ArrayList<>();

            if (existingRules != null) {
                // Populate selectedDays from existingRules
                for (CustomTimeAccess rule : existingRules) {
                    String day = rule.getDayOfWeek().name();
                    if (!selectedDays.contains(day)) {
                        selectedDays.add(day);
                    }
                }
            }

            // Now load times for these days into startTimes and endTimes maps
            loadDayTimeInputs();
        }

    }

    public void loadDayTimeInputs() {
        startTimes.clear();
        endTimes.clear();
        if (selectedDays != null) {
            for (String day : selectedDays) {
                startTimes.put(day, null);
                endTimes.put(day, null);
            }
            if (selectedEmployee != null && selectedEntrance != null) {
                List<CustomTimeAccess> existingRules = timeAccessRuleService
                        .findByEmployeeAndEntrance(selectedEmployee, selectedEntrance);

                for (CustomTimeAccess rule : existingRules) {
                    String day = rule.getDayOfWeek().name();
                    if (selectedDays.contains(day)) {
                        LocalTime start = DateFormatter.toLocalTime(rule.getStartTime());
                        LocalTime end = DateFormatter.toLocalTime(rule.getEndTime());

                        startTimes.put(day, start);
                        endTimes.put(day, end);
                    }
                }
            }
        }
    }

    public boolean validateRule() {
        boolean isValid = true;

        if (selectedEntrance == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation Error", "Select an entrance"));
            isValid = false; // critical
        }

        if (selectedDays == null || selectedDays.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Select at least one day"));
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

    public void saveDayTimeRules() {
        if (selectedEntrance == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Save Error", "Select an entrance"));
            return;
        }
        if (validateRule()) {

            timeAccessRuleService.saveOrUpdateCustomTimeAccess(selectedEmployee, selectedEntrance, startTimes, endTimes, selectedDays);

            String details = "Created a custom Entrance Time Access for: " + selectedEmployee.getFullName() + " at (" + selectedEntrance.getEntranceName() + " ).";

            auditLogService.logActivity(AuditActionType.CREATE, "Employee Profiles Page", ActionResult.SUCCESS, details, userSession.getCurrentUser());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved", "Custom time access rules saved successfully."));
            resetSidebar();
        } else {
            String details = "Failed to create a custom Entrance Time Access for " + selectedEmployee.getFullName() + "  at (" + selectedEntrance.getEntranceName() + ").";

            auditLogService.logActivity(AuditActionType.CREATE, "Employee Profiles Page", ActionResult.FAILED, details, userSession.getCurrentUser());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Saving Failed"));
        }
    }

    public void resetSidebar() {
        selectedEntrance = null;
        selectedDays = new ArrayList<>();
        startTimes = new HashMap<>();
        endTimes = new HashMap<>();
    }

    public void deleteDayTimeTule(String day) {
        if (selectedEmployee == null || selectedEntrance == null) {

            JSF.addErrorMessage("Please select an employee and entrance before removing.");
            return;
        }

        // Remove from UI model
        selectedDays.remove(day);

        startTimes.remove(day);

        endTimes.remove(day);

        // Soft delete from database
        timeAccessRuleService.deleteCustomTimeAccess(selectedEmployee, selectedEntrance, day);

        String details = "Custom Time Access deleted for " + selectedEmployee.getFullName() + " at " + selectedEntrance.getEntranceName() + " has been deleted for " + day + ".";
        auditLogService.logActivity(AuditActionType.DELETE, day, ActionResult.SUCCESS, details, userSession.getCurrentUser());

        JSF.addSuccessMessageWithSummary("Successful", "Time access for " + day + " removed successfully.");
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
    private void showMessage(FacesMessage.Severity severity, String title, String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, message));
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
    }

    // Getters and setters
    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }

    public List<Entrances> getAvailableEntrances() {
        return availableEntrances;
    }

    public void setAvailableEntrances(List<Entrances> availableEntrances) {
        this.availableEntrances = availableEntrances;
    }

    public List<Entrances> getSelectedEntrances() {
        return selectedEntrances;
    }

    public void setSelectedEntrances(List<Entrances> selectedEntrances) {
        this.selectedEntrances = selectedEntrances;
    }
}
