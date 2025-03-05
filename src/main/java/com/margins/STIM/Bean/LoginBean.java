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
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.ejb.EJB;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private String ghanaCardNumber; // Ghana Card instead of username
    private String password;
    private String errorMessage; // Add errorMessage property

    @EJB
    private User_Service userService; // Inject User Service

    // Getters & Setters
    public String getGhanaCardNumber() {
        return ghanaCardNumber;
    }

    public void setGhanaCardNumber(String ghanaCardNumber) {
        this.ghanaCardNumber = ghanaCardNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getErrorMessage() { 
        return errorMessage;
    }

    // Login method
    public String login() {
        boolean isValid = userService.validateUser(ghanaCardNumber, password);

        if (isValid) {
            errorMessage = null; // Reset error message on success
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Login successful!", null));
            return "/app/dashboard2.xhtml?faces-redirect=true"; // Redirect to dashboard
        } else {
            errorMessage = "Wrong Credentials ";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMessage, null));
            return null; // Stay on login page
        }
    }
}
