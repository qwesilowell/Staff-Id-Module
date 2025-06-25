/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("employeeRoleAssignmentBean")
@ViewScoped
public class EmployeeRoleAssignmentBean implements Serializable {

    @Inject
    private Employee_Service employeeService;

    @Inject
    private EmployeeRole_Service roleService;
    
    @Inject 
    private BreadcrumbBean breadcrumbBean;

    private List<Employee> employees;
    private List<EmployeeRole> roles;
    private Employee selectedEmployee;
    private Integer selectedRoleId;
    private String searchQuery;

    @PostConstruct
    public void init() {
        employees = employeeService.findAllEmployees();
        roles = roleService.findAllEmployeeRoles();
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<EmployeeRole> getRoles() {
        return roles;
    }
    
    public void setupBreadcrumb() {
        breadcrumbBean.setAssignRoleBreadcrumb();
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

    public void prepareAssignRole(Employee employee) {
        this.selectedEmployee = employee;

        // Update UI after setting selected employee
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", "Assigning role to "
                        + employee.getFirstname() + " " + employee.getLastname()));
    }

    public void assignRoleToEmployee() {
        if (selectedEmployee == null || selectedRoleId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "Please select an employee and a role."));
            return;
        }

        try {
            EmployeeRole role = roleService.findEmployeeRoleById(selectedRoleId);
            if (role == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Selected role does not exist."));
                return;
            }

            selectedEmployee.setRole(role);
            employeeService.updateEmployee(selectedEmployee.getGhanaCardNumber(), selectedEmployee);

            // Update employees list to reflect change
            employees = employeeService.findAllEmployees();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Role assigned successfully."));

        } catch (Exception e) {
            String errorMessage = "Error assigning role: " + e.getMessage();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", errorMessage));
            e.printStackTrace();
        }
    }





    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }

    public Integer getSelectedRoleId() {
        return selectedRoleId;
    }

    public void setSelectedRoleId(Integer selectedRoleId) {
        this.selectedRoleId = selectedRoleId;
    }
}
