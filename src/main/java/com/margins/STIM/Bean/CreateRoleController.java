/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.util.JSF;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.management.relation.Role;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.PrimeFaces;

/**
 *
 * @author PhilipManteAsare
 */
@Named("assignEmpRoleBean")
@ViewScoped
public class CreateRoleController implements Serializable {

    @Inject
    private Employee_Service employeeService;

    @Inject
    private EmployeeRole_Service roleService;
    @Inject 
    private BreadcrumbBean breadcrumbBean;

    private List<Employee> employees;
    private List<EmployeeRole> roles;
    private Employee selectedEmployee;
    private EmployeeRole selectedRole =new EmployeeRole();
    private EmployeeRole newRole = new EmployeeRole();
    @Getter @Setter
    private String searchQuery;
    
    @Getter @Setter
    private List<Role> filteredRoles;
    @Getter @Setter
private String globalFilter;

    @PostConstruct
    public void init() {
        employees = employeeService.findAllEmployees();
        roles = roleService.findAllEmployeeRoles();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setCreateRoleBreadcrumb();
    }


    public void addNewRole() {
        if (newRole.getRoleName() != null && !newRole.getRoleName().isEmpty()) {
            
            newRole.setRoleName(newRole.getRoleName().trim());
            
            roleService.createEmployeeRole(newRole);
            refreshRoles();
            
            newRole = new EmployeeRole();
            PrimeFaces.current().ajax().update("roleForm");
            
            showMessage(FacesMessage.SEVERITY_INFO, "Success", "New role added successfully!");
        } else {
            showMessage(FacesMessage.SEVERITY_WARN, "Warning", "Role Name cannot be empty.");
        }
    }

    public void prepareEditRole(EmployeeRole role) {
        this.selectedRole = role;
    }
    
    
    public void updateRole(){
        try {
            roleService.updateEmployeeRole(selectedRole.getId(), selectedRole);// Update existing access level
            JSF.addSuccessMessage("Success " + selectedRole.getRoleName() +" updated successfully!");
            refreshRoles(); // Refresh the roles list
            newRole = new EmployeeRole(); // Reset form
        } catch (Exception e) {
            JSF.addErrorMessage( "Failed to update role.");
            e.printStackTrace();
        }
    }
    
    public void findRole() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // If no search query, fetch all roles
            roles = roleService.findAllEmployeeRoles();
        } else {
            // Search roles by name
            roles = roleService.findEmployeeRoleByName(searchQuery);
        }
    }
    
    public void deleteRole(EmployeeRole role ) {
 
        try {
            roleService.deleteEmployeeRole(role.getId()); // Delete the employee role
          JSF.addSuccessMessage( "Role "+role.getRoleName()+" deleted successfully!");
            refreshRoles(); // Refresh the list
            newRole = new EmployeeRole();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete "+ role.getRoleName()));
            e.printStackTrace();
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
