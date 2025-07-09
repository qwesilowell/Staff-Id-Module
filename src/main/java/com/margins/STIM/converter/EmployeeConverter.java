/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.Employee_Service;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter(value = "employeeConverter", managed = true)
public class EmployeeConverter implements Converter<Employee> {

    @Inject
    private Employee_Service employeeService;

    @Override
    public Employee getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Parse the String to an int
            int employeeId = Integer.parseInt(value);
            return employeeService.findEmployeeById(employeeId);
        } catch (NumberFormatException e) {
            // Handle the case where the value cannot be parsed to an int
            return null;
        } catch (Exception e) {
            // Handle other potential exceptions
            return null;
        }

    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Employee employee) {
        if (employee == null || employee.getId() == 0) {
            return "";
        }
        return String.valueOf(employee.getId());
    }
}
