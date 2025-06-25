package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

@Named("assignEntranceBean")
@SessionScoped
public class AssignEntranceBean implements Serializable {

    @EJB
    private Employee_Service employeeService;

    @EJB
    private EntrancesService entrancesService;
    
    @Inject
    private BreadcrumbBean breadcrumbBean;

    private String searchQuery;
    private List<Employee> employees;
    private Employee selectedEmployee;
    private List<Entrances> availableEntrances;
    private List<Entrances> selectedEntrances;
    @Getter
    @Setter
    private List<Entrances> assignedCustomEntrances;
    @Getter
    @Setter
    private List<Entrances> assignedRoleEntrances;

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
            assignedRoleEntrances.forEach(e -> roleEntranceIds.add(e.getEntrance_Device_ID()));
        }
        if (assignedCustomEntrances != null) {
            assignedCustomEntrances.forEach(e -> roleEntranceIds.add(e.getEntrance_Device_ID()));
        }
        return availableEntrances.stream()
                .filter(e -> !roleEntranceIds.contains(e.getEntrance_Device_ID()))
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
                    .map(e -> e.getEntrance_Device_ID() + ":" + e.getEntrance_Name())
                    .collect(Collectors.joining(", ")));

            // Fetch existing entrances
            List<Entrances> currentEntrances;
            List<Entrances> customEntrances = selectedEmployee.getCustomEntrances();
            if (customEntrances != null) {
                currentEntrances = new ArrayList<>(customEntrances);
            } else {
                currentEntrances = new ArrayList<>();
            }

            // Add only new ones that aren't already in the list
            for (Entrances selected : selectedEntrances) {
                boolean exists = currentEntrances.stream()
                        .anyMatch(e -> e.getEntrance_Device_ID().equals(selected.getEntrance_Device_ID()));
                if (!exists) {
                    currentEntrances.add(selected);
                }
            }

            // Save merged list
            selectedEmployee.setCustomEntrances(currentEntrances);
            employeeService.updateEmployeeEnt(selectedEmployee.getGhanaCardNumber(), selectedEmployee);

            // Update UI list
            assignedCustomEntrances = new ArrayList<>(currentEntrances);
            employees = employeeService.findAllEmployees(); // Refresh list

            showMessage(FacesMessage.SEVERITY_INFO, "Success", "Custom entrances updated for " + selectedEmployee.getFullName());

            // Clear state
            selectedEmployee = null;
            selectedEntrances.clear();

        } catch (EntityNotFoundException e) {
            logError("Employee not found", e);
            showMessage(FacesMessage.SEVERITY_ERROR, "Error", "Employee not found: " + selectedEmployee.getGhanaCardNumber());
        } catch (Exception e) {
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
            System.out.println("Removing entrance " + entrance.getEntrance_Device_ID() + " for employee: " + selectedEmployee.getGhanaCardNumber());

            // Remove entrance from the list
            selectedEntrances.removeIf(e -> e.getEntrance_Device_ID().equals(entrance.getEntrance_Device_ID()));
            assignedCustomEntrances.removeIf(e -> e.getEntrance_Device_ID().equals(entrance.getEntrance_Device_ID()));

            // Sync with employee object
            selectedEmployee.setCustomEntrances(new ArrayList<>(assignedCustomEntrances));

            // Update in database
            employeeService.updateEmployee(selectedEmployee.getGhanaCardNumber(), selectedEmployee);

            availableEntrances = getAllEntrances();

          
            showMessage(FacesMessage.SEVERITY_WARN, "Success", "Removed custom entrance \"" + entrance.getEntrance_Name() + "\" for " + selectedEmployee.getFullName());
        } catch (Exception e) {
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
