/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.service.User_Service;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter(value = "viewPermissionConverter", managed = true)
public class ViewPermissionConverter implements Converter<ViewPermission> {

    @Inject
    private User_Service ViewPermissionService;

    @Override
    public ViewPermission getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            int viewId = Integer.parseInt(value);
            return ViewPermissionService.findPageById(viewId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ViewPermission view) {
        if (view == null) {
            return "";
        }
        return String.valueOf(view.getId());
    }
}


