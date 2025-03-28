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
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("manageEmployeesBean")
@ViewScoped
public class ManageEmployeesBean implements Serializable {

    private List<Employee> employees;
    private String searchTerm;

    @Getter
    @Setter
    private String searchQuery;
    
    @Inject
    private Employee_Service employeeService;

    @PostConstruct
    public void init() {
        employees = employeeService.findAllEmployees();
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
