/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.service.EmployeeRole_Service;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter(value = "employeeRoleConverter", managed = true)
public class EmployeeRoleConverter implements Converter<EmployeeRole> {

    @Inject
    private EmployeeRole_Service roleService;

    @Override
    public EmployeeRole getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            int roleId = Integer.parseInt(value);
            return roleService.findEmployeeRoleById(roleId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, EmployeeRole role) {
        if (role == null) {
            return "";
        }
        return String.valueOf(role.getId());
    }
}
