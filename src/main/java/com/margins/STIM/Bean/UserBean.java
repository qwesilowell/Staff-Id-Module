package com.margins.STIM.Bean;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import jakarta.ejb.EJB;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.entity.Users;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt; // Import for password hashing

@Named("userBean")
@SessionScoped
public class UserBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @EJB
    private User_Service userService;
    
    @NotEmpty(message = "Ghana Card Number is required")
    private String ghanaCardNumber;
    
    @NotEmpty(message = "Username is required")
    private String username;
    
    @NotEmpty(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    
    @NotEmpty(message = "Please confirm your password")
    private String confirmPassword;
    
    @NotEmpty(message = "Role is required")
    private String userRole;
    
    @Getter
    @Setter
    private List<Users> users = new ArrayList<>();
    
    public void getUserss() {
        users = userService.findAllUsers();
        System.out.println("USERS>>>>>>>>>>>>>> " + users.toString());
    }

    // Register method
    public String register() {
        try {
            // Check if passwords match
            if (!password.equals(confirmPassword)) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Passwords do not match", null));
                return null;
            }
            
            // Check if Ghana Card Number already exists
            if (userService.findUserByGhanaCard(ghanaCardNumber) != null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Ghana Card Number already registered", null));
                return null;
            }
            
            // Hash the password before storing
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            // Create new user entity
            Users newUser = new Users();
            newUser.setGhanaCardNumber(ghanaCardNumber);
            newUser.setUsername(username); // Store username
            newUser.setPassword(hashedPassword); // Store hashed password
            newUser.setUserRole(userRole);
            
            // Save user to database
            userService.createUser(newUser);
            
            // Success message
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Registration successful! Please login.", null));
            
            // Clear form
            clearForm();
            
            // Redirect to login page
            return "/login.xhtml?faces-redirect=true";
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Error during registration: " + e.getMessage(), null));
            return null;
        }
    }
    
    private void clearForm() {
        this.ghanaCardNumber = null;
        this.username = null;
        this.password = null;
        this.confirmPassword = null;
        this.userRole = null;
    }
    
    // Getters and Setters
    public String getGhanaCardNumber() {
        return ghanaCardNumber;
    }

    public void setGhanaCardNumber(String ghanaCardNumber) {
        this.ghanaCardNumber = ghanaCardNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
