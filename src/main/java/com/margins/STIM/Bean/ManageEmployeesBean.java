/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author PhilipManteAsare
 */
@Named("manageEmployeesBean")
@ViewScoped
public class ManageEmployeesBean implements Serializable {

    private List<Employee> employees;
    private String searchTerm;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;

    @Inject
    private EntrancesService entrancesService;
    @Inject
    private Employee_Service employeeService;
    @Inject
    private TimeAccessRuleService timeAccessRuleService;

    @Getter
    @Setter
    private Employee selectedEmployee;
    @Getter
    @Setter
    private Entrances selectedEntrance;
    @Getter
    @Setter
    private String searchQuery;
    @Getter
    @Setter
    private Set<Entrances> selectedEmployeeEntrances;
    @Getter
    @Setter
    private Set<Entrances> selectedCustomEmpEntrances;
    @Getter
    @Setter
    private boolean showEntrances;
    @Getter
    @Setter
    private boolean showCustomEntrances;
    @Getter
    @Setter
    private boolean showCustomTimeRules;
    @Getter
    private List<Entrances> allEntrances;
    @Getter
    @Setter
    private CustomTimeAccess customRule;

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
        employees = employeeService.findAllEmployees();
        allEntrances = entrancesService.findAllEntrances();
        selectedEmployeeEntrances = new HashSet<>();
        selectedCustomEmpEntrances = new HashSet<>();
        showEntrances = false;
        showCustomEntrances = false;
        showCustomTimeRules = false;
    }

    public void setupBreadcrumb() {
        if (breadcrumbBean != null) {
            breadcrumbBean.setEmployeeListBreadcrumb();
            System.out.println("Breadcrumb set successfully.");
        } else {
            System.out.println("breadcrumbBean is null in setupBreadcrumb()");
        }
    }

    public void findEmployee() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            employees = employeeService.findAllEmployees();
        } else {
            employees = employeeService.findAllEmployees().stream()
                    .filter(emp -> emp.getGhanaCardNumber().contains(searchQuery)
                    || (emp.getFirstname() + " " + emp.getLastname()).toLowerCase().contains(searchQuery.toLowerCase()))
                    .collect(Collectors.toList());
        }
    }

    public void searchEmployees() {
        if (searchTerm == null || searchTerm.isEmpty()) {
            employees = employeeService.findAllEmployees();
        } else {
            employees = employeeService.searchEmployeesByGhanaCard(searchTerm);
        }
    }

    public void viewEmployee(String ghanaCardNumber) {
        selectedEmployee = employeeService.findEmployeeByGhanaCard(ghanaCardNumber);
        selectedEntrance = null;
        showEntrances = false;
        showCustomEntrances = false;
        showCustomTimeRules = true;
        selectedDays = new ArrayList<>();
        startTimes = new HashMap<>();
        endTimes = new HashMap<>();

        loadEntrancesForRole();
    }

    public void deleteEmployee() {
        if (selectedEmployee != null) {
            try {
                System.out.println("Deleting employee: " + selectedEmployee.getGhanaCardNumber());
                employeeService.deleteEmployee(selectedEmployee.getGhanaCardNumber());

                String details = "Deleted employee: " + selectedEmployee.getFullName() + " with GhanaCardNumber " + selectedEmployee.getGhanaCardNumber();

                auditLogService.logActivity(AuditActionType.DELETE, "Employee Profile Page", ActionResult.SUCCESS, details, userSession.getCurrentUser());

                employees = employeeService.findAllEmployees(); // Refresh the list
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employee " + selectedEmployee.getFullName() + " deleted."));
                selectedEmployee = null; // Clear selection
            } catch (Exception e) {
                String details = "Failed to delete employee "+selectedEmployee.getFullName() + " with GhanaCardNumber " + selectedEmployee.getGhanaCardNumber()  + " reason: " + e.getMessage();
                auditLogService.logActivity(AuditActionType.DELETE, "Employee Profile Page", ActionResult.FAILED, details, userSession.getCurrentUser());

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete employee: " + e.getMessage()));
                e.printStackTrace();
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No employee selected."));
        }
    }

    public String getFullName() {
        if (selectedEmployee != null) {
            String first = selectedEmployee.getFirstname();
            String last = selectedEmployee.getLastname();
            return (first != null ? first : "") + " " + (last != null ? last : "");
        }
        return "No Employee Selected";
    }

    public String getSelectedEmployeeInitials() {
        if (selectedEmployee == null || selectedEmployee.getFirstname() == null || selectedEmployee.getLastname() == null) {
            return "??"; // Default initials if no employee is selected
        }

        // Extract initials from first and last name
        String firstInitial = selectedEmployee.getFirstname().substring(0, 1).toUpperCase();
        String lastInitial = selectedEmployee.getLastname().substring(0, 1).toUpperCase();

        return firstInitial + lastInitial;
    }

    public void loadEntrancesForRole() {
        selectedEmployeeEntrances.clear(); // clear old data first

        if (selectedEmployee != null && selectedEmployee.getRole() != null) {
            Set<Entrances> entrances = selectedEmployee.getRole().getAccessibleEntrances();

            if (entrances != null && !entrances.isEmpty()) {
                selectedEmployeeEntrances.addAll(entrances);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No entrances assigned to this role."));
            }
        }
    }

    public void loadCustomEntrances() {
        selectedCustomEmpEntrances.clear(); // Clear existing entries
        if (selectedEmployee != null) {
            List<Entrances> customEntrances = selectedEmployee.getCustomEntrances();

            if (customEntrances != null && !customEntrances.isEmpty()) {
                selectedCustomEmpEntrances.clear();
                selectedCustomEmpEntrances.addAll(customEntrances);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No custom entrances assigned to this employee."));
            }
        }
    }

    public void toggleEntrances() {
        showEntrances = !showEntrances;
    }

    public void toggleCustomEntrances() {
        showCustomEntrances = !showCustomEntrances;
        if (showCustomEntrances) {
            loadCustomEntrances();
        }
    }

    public List<Entrances> searchCustomEntrances(String query) {
        return selectedEmployee.getCustomEntrances().stream()
                .filter(e -> e.getEntranceName() != null
                && e.getEntranceName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Entrances> searchAllEntrances(String query) {
        return allEntrances.stream()
                .filter(e -> e.getEntranceName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
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

    public boolean validateRule() {
        boolean isValid = true;

        if (selectedEmployee == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Validation Error", "Select an entrance"));
            return false; // critical
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

    public void resetSidebar() {
//        selectedEntranceId = null;
        selectedEntrance = null;
        selectedDays = new ArrayList<>();
        startTimes = new HashMap<>();
        endTimes = new HashMap<>();
    }

    public void toggleCustomTimeRules() {
        if (selectedEmployee == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No employee selected"));
            return;
        }
        showCustomTimeRules = !showCustomTimeRules;
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

    public void saveDayTimeRules() {
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
        
        String details = "Custom Time Access deleted for " +selectedEmployee.getFullName()+ " at " + selectedEntrance.getEntranceName() + " has been deleted for " + day + ".";
        auditLogService.logActivity(AuditActionType.DELETE, day, ActionResult.SUCCESS, details, userSession.getCurrentUser());

        JSF.addSuccessMessageWithSummary("Successful", "Time access for " + day + " removed successfully.");
    }

    // Getters and Setters
    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String goToSignup() {
        return "/app/OnboardEmployee/onboardEmployee.xhtml?faces-redirect=true";
    }
}
