/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */

import com.margins.STIM.service.User_Service;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Named("sublogin")
@SessionScoped
public class sublogincontroller implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String ghanaCardNumber;
    private String password;
    @EJB
    private User_Service userService;
    
 
    // Login method
    public String login() {
        if ("GHA-726682342-4".equals(ghanaCardNumber) && "admin123".equals(password)) {
            String username = "Philip Mante Asare"; // Hardcoded full name  
            String userRole = "Admin"; // Hardcoded role  

            // Store session values  
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("username", username);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userRole", userRole);
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("ghanaCardNumber", ghanaCardNumber);

            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("app/dashboard2.xhtml");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null; // Prevent JSF from navigating elsewhere  
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid credentials", "Ghana Card number or password is incorrect");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return null;
        }
    }
    
    // Logout method
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }
    
    public String getUserInitials() {
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");

        if (username == null || username.trim().isEmpty()) {
            return "??"; // Return default initials if username is missing
        }

        // Trim and split the name
        String[] nameParts = username.trim().split("\\s+"); // Split by one or more spaces
        if (nameParts.length == 1) {
            return nameParts[0].substring(0, 1).toUpperCase(); // Single name case
        }

        // Take first letter of first and last name
        return nameParts[0].substring(0, 1).toUpperCase() + nameParts[nameParts.length - 1].substring(0, 1).toUpperCase();
    }
}   

