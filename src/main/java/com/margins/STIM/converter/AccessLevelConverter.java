/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.Access_Levels;
import com.margins.STIM.service.AccessLevelsService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(value = "accessLevelConverter", managed = true) // Use 'managed = true' for CDI injection
public class AccessLevelConverter implements Converter<Access_Levels> {

    @Inject 
    private AccessLevelsService accessLevelsService;

    @Override
    public String getAsString(FacesContext context, UIComponent component, Access_Levels accessLevel) {
        if (accessLevel == null) {
            return "";
        }
        return String.valueOf(accessLevel.getId()); // Convert object to its ID
    }

    @Override
    public Access_Levels getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(value); // Convert String to int
            return accessLevelsService.findAccessLevelById(id); // Retrieve the object
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Access Level ID: " + value, e);
        }
    }
    
}
