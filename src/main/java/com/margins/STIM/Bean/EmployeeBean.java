/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.BiometricData;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.service.Employee_Service;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ejb.EJB;
import java.io.Serializable;


@Named
@ViewScoped
public class EmployeeBean implements Serializable {

    private Employee employee = new Employee();
    private BiometricData biometricData = new BiometricData();

    @EJB
    private Employee_Service employeeService;

    public void registerEmployee() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            biometricData.setEmployee(employee); // Link biometric data to employee
            employee.setBiometricData(biometricData);
            employeeService.registerEmployee(employee, biometricData);

            showGrowlMessage("Success", "Employee registered successfully!", FacesMessage.SEVERITY_INFO);

            // Reset form
            employee = new Employee();
            biometricData = new BiometricData();
        } catch (Exception e) {
            showGrowlMessage("Error", "Registration failed: " + e.getMessage(), FacesMessage.SEVERITY_ERROR);
        }
    }

    private void showGrowlMessage(String summary, String detail, FacesMessage.Severity severity) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters and Setters
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public BiometricData getBiometricData() { return biometricData; }
    public void setBiometricData(BiometricData biometricData) { this.biometricData = biometricData; }
}
