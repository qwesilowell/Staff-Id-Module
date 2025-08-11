/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.converter;

import com.margins.STIM.entity.Users;
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
@FacesConverter(value = "userConverter", managed = true)
public class UserConverter implements Converter<Users> {

    @Inject
    private User_Service userService;

    @Override
    public Users getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            int id = Integer.parseInt(value);
            return userService.findUserById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Users user) {
        if (user == null || user.getId() == 0) {
            return "";
        }
        return String.valueOf(user.getId());    }
}
