/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.service.Employee_Service;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

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
    private Employee_Service employeeService;

    @PostConstruct
    public void init() {
        employees = employeeService.findAllEmployees();
    }

    public void searchEmployees() {
        if (searchTerm == null || searchTerm.isEmpty()) {
            employees = employeeService.findAllEmployees();
        } else {
            employees = employeeService.searchEmployeesByGhanaCard(searchTerm);
        }
    }

    public String viewEmployee(String ghanaCardNumber) {
        return "viewEmployee.xhtml?faces-redirect=true&amp;ghanaCardNumber=" + ghanaCardNumber;
    }

    public String editEmployee(String ghanaCardNumber) {
        return "editEmployee.xhtml?faces-redirect=true&amp;ghanaCardNumber=" + ghanaCardNumber;
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
