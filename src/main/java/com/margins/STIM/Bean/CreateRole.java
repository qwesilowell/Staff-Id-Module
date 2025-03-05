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
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 *
 * @author PhilipManteAsare
 */
@Named("assignEmpRoleBean")
@ViewScoped
public class CreateRole implements Serializable {

    @Inject
    private Employee_Service employeeService;

    @Inject
    private EmployeeRole_Service roleService;

    private List<Employee> employees;
    private List<EmployeeRole> roles;
    private Employee selectedEmployee;
    private EmployeeRole selectedRole;
    private EmployeeRole newRole = new EmployeeRole();

    @PostConstruct
    public void init() {
        employees = employeeService.findAllEmployees();
        roles = roleService.findAllEmployeeRoles();
    }

//    public void assignRoleToEmployee() {
//    if (selectedEmployee != null && selectedRole != null) {
//        List<EmployeeRole> selectedRoles = new ArrayList<>();
//        roles.add(selectedRole);  // Add the selectedRole to a list
//
//        employeeService.assignRolesToEmployee(selectedEmployee.getGhanaCardNumber(), roles);
//        showMessage(FacesMessage.SEVERITY_INFO, "Success", "Role assigned successfully!");
//    } else {
//        showMessage(FacesMessage.SEVERITY_WARN, "Warning", "Please select an Employee and a Role.");
//    }
//}


    public void addNewRole() {
        if (newRole.getRoleName() != null && !newRole.getRoleName().isEmpty()) {
            roleService.createEmployeeRole(newRole);
            refreshRoles();
            newRole = new EmployeeRole();
            showMessage(FacesMessage.SEVERITY_INFO, "Success", "New role added successfully!");
        } else {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "Role Name cannot be empty.");
        }
    }

    private void showMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    private void refreshRoles() {
        roles = roleService.findAllEmployeeRoles();
    }

    // Getters and Setters
    public List<Employee> getEmployees() {
        return employees;
    }

    public List<EmployeeRole> getRoles() {
        return roles;
    }

    public Employee getSelectedEmployee() {
        return selectedEmployee;
    }

    public void setSelectedEmployee(Employee selectedEmployee) {
        this.selectedEmployee = selectedEmployee;
    }

    public EmployeeRole getSelectedRole() {
        return selectedRole;
    }

    public void setSelectedRole(EmployeeRole selectedRole) {
        this.selectedRole = selectedRole;
    }

    public EmployeeRole getNewRole() {
        return newRole;
    }

    public void setNewRole(EmployeeRole newRole) {
        this.newRole = newRole;
    }
}
