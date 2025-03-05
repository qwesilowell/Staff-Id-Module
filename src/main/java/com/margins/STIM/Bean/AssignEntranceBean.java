/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.*;
import com.margins.STIM.service.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named("assignEntranceBean")
@ViewScoped
public class AssignEntranceBean implements Serializable {

    @EJB
    private Employee_Service employeeService;
    @EJB
    private AccessLevelsService accessLevelsService;
    @EJB
    private EntrancesService entranceService;
    @EJB
    private EmployeeEntranceService employeeEntranceService;

    private List<Employee> employees = new ArrayList<>();
    private List<Entrances> entrances = new ArrayList<>();
    private List<EmployeeEntrance> assignedEntrances = new ArrayList<>();
    private List<Access_Levels> accessLevels = new ArrayList<>();
    private Employee selectedEmployee;
    private String searchQuery;

    @PostConstruct
    public void init() {
        try {
            employees = employeeService.findAllEmployees();
            entrances = entranceService.findAllEntrances();
            accessLevels = accessLevelsService.findAllAccessLevels();
        } catch (Exception e) {
            logError("Error initializing data", e);
        }
    }

    public void searchEmployees() {
        employees = (searchQuery == null || searchQuery.trim().isEmpty())
                ? employeeService.findAllEmployees()
                : employeeService.searchEmployees(searchQuery);
    }

    public void prepareAssignEntrance(Employee employee) {
        if (employee == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Employee cannot be null!");
            return;
        }
        this.selectedEmployee = employee;
        try {
            assignedEntrances = employeeEntranceService.getEmployeeEntrancesByGhanaCardNumber(employee.getGhanaCardNumber());
        } catch (Exception e) {
            logError("Error fetching assigned entrances", e);
            assignedEntrances = new ArrayList<>();
        }
    }

    public void confirmAssignEntrance(Entrances entrance) {
        if (assignedEntrances.stream()
                .anyMatch(e -> e.getEntrance().getEntrance_Device_ID().equals(entrance.getEntrance_Device_ID()))) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "Entrance already assigned!");
            return;
        }
        assignedEntrances.add(new EmployeeEntrance(selectedEmployee, entrance, new Access_Levels()));
    }

    public void removeAssignedEntrance(EmployeeEntrance assignedEntrance) {
        assignedEntrances.removeIf(entry
                -> entry.getEntrance().getEntrance_Device_ID().equals(assignedEntrance.getEntrance().getEntrance_Device_ID()));
        showMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrance removed successfully!");
    }

    public void saveEntrances() {
        if (validateAssignedEntrances()) {
            executeAssignment(() -> employeeEntranceService.saveEmployeeEntrances(selectedEmployee, assignedEntrances), "Entrances assigned successfully!");
        }
    }

    public void updateEntrances() {
        if (validateAssignedEntrances()) {
            executeAssignment(() -> employeeEntranceService.updateEmployeeEntrances(selectedEmployee, assignedEntrances), "Entrances updated successfully!");
        }
    }

    public boolean hasAssignedEntrances(Employee employee) {
        return employee != null && employee.getGhanaCardNumber() != null
                && !employeeEntranceService.getAssignedEntrances(employee.getGhanaCardNumber()).isEmpty();
    }

    public void openAssignPopup(Employee employee) {
        if (employee == null) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "No employee selected!");
            return;
        }
        this.selectedEmployee = employee;
        assignedEntrances = employeeEntranceService.getEmployeeEntrancesByGhanaCardNumber(employee.getGhanaCardNumber());
        if (assignedEntrances == null) {
            assignedEntrances = new ArrayList<>();
        }
    }

    public void addEntranceToEmployee(Entrances entrance) {
        if (selectedEmployee == null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "No employee selected!");
            return;
        }
        if (assignedEntrances.stream().anyMatch(e -> e.getEntrance().getEntrance_Device_ID().equals(entrance.getEntrance_Device_ID()))) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "Entrance already assigned!");
            return;
        }
        assignedEntrances.add(new EmployeeEntrance(selectedEmployee, entrance, accessLevels.get(0))); // Default access level
    }

    public void handleSaveOrUpdate() {
        if (selectedEmployee == null) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "No employee selected!");
            return;
        }
        if (hasAssignedEntrances(selectedEmployee)) {
            updateEntrances();
        } else {
            saveEntrances();
        }
    }

    private void showMessage(FacesMessage.Severity severity, String title, String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, title, message));
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
    }

    private boolean validateAssignedEntrances() {
        if (assignedEntrances == null || assignedEntrances.isEmpty()) {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "No entrances assigned!");
            return false;
        }
        return true;
    }

    private void executeAssignment(Runnable assignment, String successMessage) {
        try {
            assignment.run();
            assignedEntrances.clear();
            showMessage(FacesMessage.SEVERITY_INFO, "Success", successMessage);
        } catch (Exception e) {
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to assign entrances.");
            logError("Assignment error", e);
        }
    }

    // Getters & Setters
    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Entrances> getEntrances() {
        return entrances;
    }

    public List<EmployeeEntrance> getAssignedEntrances() {
        return assignedEntrances;
    }

    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public List<Access_Levels> getAccessLevels() {
        return accessLevels;
    }
}
