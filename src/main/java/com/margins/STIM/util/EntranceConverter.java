/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import com.margins.STIM.entity.Entrances;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter("entranceConverter")
public class EntranceConverter implements Converter<Entrances> {

    @Override
    public Entrances getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        List<Entrances> entrances = context.getApplication().evaluateExpressionGet(context, "#{dashboardBean.allEntrances}", List.class);
        return entrances.stream()
                .filter(e -> e.getEntrance_Device_ID().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Entrances value) {
        return value == null ? "" : value.getEntrance_Device_ID();
    }
}
