/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.Devices;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

/**
 *
 * @author PhilipManteAsare
 */
@FacesConverter(value = "deviceConverter", managed = true)
public class DeviceConverter implements Converter<Devices> {

    @Inject
    private DeviceService deviceService;

    @Override
    public Devices getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(value);
            return deviceService.findDeviceById(id);
        } catch (NumberFormatException e) {
            System.err.println("Invalid device ID: " + value);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Devices device) {
        if (device == null){
        return "";
        }
        return String.valueOf(device.getId());
    }
} 
