/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.EntrancesService;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter(value = "entranceConverter", managed = true)
public class EntranceConverter implements Converter<Entrances> {

    @Inject
    private EntrancesService entrancesService;

    @Override
    public Entrances getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Parse the String to an int
            int entranceId = Integer.parseInt(value);
            return entrancesService.findEntranceById(entranceId);
        } catch (NumberFormatException e) {
            // Handle the case where the value cannot be parsed to an int
            return null;
        } catch (Exception e) {
            // Handle other potential exceptions
            return null;
        }
    
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Entrances entrance) {
        if (entrance == null || entrance.getId() == 0) {
            return "";
        }
        return String.valueOf(entrance.getId());
    }
}
