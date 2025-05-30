/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.CustomTimeAccess;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.TimeAccessRule;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.TimeAccessRuleService;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    @Setter
    private String selectedEntranceId;
    @Getter
    private List<Entrances> allEntrances;
    @Getter
    @Setter
    private List<String> newRuleDays;
    @Getter
    @Setter
    private TimeAccessRule newRule;
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
        newRuleDays = new ArrayList<>();
        selectedEmployeeEntrances = new HashSet<>();
        selectedCustomEmpEntrances = new HashSet<>();
        showEntrances = false;
        showCustomEntrances = false;
        showCustomTimeRules = false;
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
        selectedEntranceId = null;
        selectedEntrance = null;
        newRule = new TimeAccessRule();
        newRuleDays = new ArrayList<>();
        showEntrances = false;
        showCustomEntrances = false;
        showCustomTimeRules = true;

        loadEntrancesForRole();
    }

    public void deleteEmployee() {
        if (selectedEmployee != null) {
            try {
                System.out.println("Deleting employee: " + selectedEmployee.getGhanaCardNumber());
                employeeService.deleteEmployee(selectedEmployee.getGhanaCardNumber());
                employees = employeeService.findAllEmployees(); // Refresh the list
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employee " + selectedEmployee.getFullName() + " deleted."));
                selectedEmployee = null; // Clear selection
            } catch (Exception e) {
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

    public List<Entrances> searchEntrances(String query) {
        return selectedEmployee.getCustomEntrances().stream()
                .filter(e -> e.getEntrance_Name() != null
                && e.getEntrance_Name().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Entrances> searchAllEntrances(String query) {
        return allEntrances.stream()
                .filter(e -> e.getEntrance_Name().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void onEntranceSelect() {
        if (selectedEntranceId != null && !selectedEntranceId.trim().isEmpty()) {
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
            if (selectedEntrance != null && selectedEmployee != null) {
                prepareTimeRule(selectedEmployee);
            }
        }
    }

    public void prepareTimeRule(Employee selectedEmployee) {
        newRule = new TimeAccessRule();
        newRule.setEmployee(selectedEmployee);
        this.selectedEmployee = selectedEmployee;
        newRuleDays = new ArrayList<>();

        if (selectedEntranceId != null) {
            selectedEntrance = entrancesService.findEntranceById(selectedEntranceId);
            newRule.setEntrance(selectedEntrance);
            List<TimeAccessRule> existingRules = timeAccessRuleService.getTimeRulesByEmployee(selectedEmployee.getGhanaCardNumber());
            existingRules.stream()
                    .filter(r -> r.getEntrance().getEntrance_Device_ID().equals(selectedEntranceId))
                    .findFirst()
                    .ifPresent(existingRule -> {
                        newRule = existingRule;
                        if (existingRule.getDaysOfWeek() != null && !existingRule.getDaysOfWeek().isEmpty()) {
                            newRuleDays = new ArrayList<>(Arrays.asList(existingRule.getDaysOfWeek().split(",")));
                        }
                    });
        }

    }

    public void prepareCustomTimeRules(SelectEvent<String> event) {
        String entranceId = event.getObject();
        this.selectedEntranceId = entranceId;
        this.selectedEntrance = entrancesService.findEntranceById(entranceId);

        if (selectedEmployee != null) {
            customRule = new CustomTimeAccess();
            customRule.setEmployee(selectedEmployee);
            customRule.setEntrances(selectedEntrance);
        }
    }


public void saveTimeRule() {

        if (newRule.getEntrance() == null) {
            newRule.setEntrance(selectedEntrance);
        }

        newRule.setDaysOfWeek(String.join(",", newRuleDays));
        timeAccessRuleService.saveTimeRule(newRule);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", " Custom Time rule saved successfully!"));
        resetSidebar();
    }

    public boolean validateRule() {
        boolean isValid = true;

        if (selectedEntranceId == null || selectedEmployee == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Select an employee and entrance"));
            isValid = false;
        }
        if (newRule.getStartTime() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Start time is required"));
            isValid = false;
        }
        if (newRule.getEndTime() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "End time is required"));
            isValid = false;
        }
        if (newRuleDays == null || newRuleDays.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Validation Error", "Select at least one day"));
            isValid = false;
        }
        // Proceed only if both times are set
        if (newRule.getStartTime() != null && newRule.getEndTime() != null) {
            Date startDate = newRule.getStartTime();
            Date endDate = newRule.getEndTime();

            LocalTime startTime = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
            LocalTime endTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

            if (endTime.equals(startTime)) {
                FacesContext.getCurrentInstance().addMessage(null,
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
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Validation Error",
                                "End time must be after start time"));
                isValid = false;
            }
        }
        return isValid;
    }

    public void resetSidebar() {
        newRule = new TimeAccessRule();
        newRuleDays = new ArrayList<>();
        selectedEntranceId = null;
        selectedEntrance = null;
    }

    public void toggleCustomTimeRules() {
        if (selectedEmployee == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No employee selected"));
            return;
        }
        showCustomTimeRules = !showCustomTimeRules;
    }
    
    public List<TimeAccessRule> getEmployeeTimeRules(String ghanaCardNumber) {
        return timeAccessRuleService.getTimeRulesByEmployee(ghanaCardNumber);
    }
    
    public String formatTimeRule(TimeAccessRule rule) {
        if (rule == null || rule.getEntrance() == null) {
            return "No time rule";
        }
        String timeRange = formatTimeRange(rule.getStartTime(), rule.getEndTime(), rule.getDaysOfWeek());
        return rule.getEntrance().getEntrance_Name() + ": " + timeRange;
    }

    private String formatTimeRange(Date start, Date end, String daysCsv) {
        if (start == null || end == null) {
            return "-";
        }
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String formattedStart = timeFormat.format(start);
        String formattedEnd = timeFormat.format(end);
        String days = formatDays(daysCsv);
        return formattedStart + " - " + formattedEnd + " (" + days + ")";
    }

    private String formatDays(String daysCsv) {
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
    
    public void loadDayTimeInputs() {
        startTimes.clear();
        endTimes.clear();
        if (selectedDays != null) {
            for (String day : selectedDays) {
                startTimes.putIfAbsent(day, null);
                endTimes.putIfAbsent(day, null);
            }

            System.out.println("Selected Days>>>>>>" + selectedDays);
        }
    }
    
    public void saveDayTimeRules() {
        if (selectedEmployee != null && selectedEntrance != null && selectedDays != null) {
            timeAccessRuleService.saveOrUpdateCustomTimeAccess(selectedEmployee, selectedEntrance, startTimes, endTimes, selectedDays);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Saved", "Day time access rules saved successfully."));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Missing role, entrance, or selected days."));
        }
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
