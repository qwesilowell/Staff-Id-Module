/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.entity.SystemUserRoles;
import com.margins.STIM.service.UserRolesServices;
import jakarta.ejb.EJB;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter(value = "systemUserRoleConverter", managed = true)
public class SystemUserRoleConverter implements Converter<SystemUserRoles> {

    @EJB
    private UserRolesServices roleService;

    @Override
    public SystemUserRoles getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            return null;
        }

        try {
            int id = Integer.parseInt(value);
            return roleService.findUserRoleById(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid role ID: " + value, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, SystemUserRoles value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value.getId());
    }
}
