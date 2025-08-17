/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.Seeder.DummyDataSeeder;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.enums.UserStatus;
import com.margins.STIM.service.ActivityLogService;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Named("sublogin")
@SessionScoped
public class Sublogincontroller implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ghanaCardNumber;
    private String password;
    private String email;
    private boolean showPasswordChangeModal = false;
    private String newPassword;
    private String confirmPassword;
    @EJB
    private User_Service userService;

    @EJB
    private ActivityLogService activityLogService;

    @Inject
    private AuditLogService auditLogService;

    @Inject
    private DummyDataSeeder dummyDataSeeder;

    @Inject
    private UserSession userSession;

    // Login method
    public String login() {
        try {

            if (allowLogin()) {
                if (handleSuperAdmin()) {

                    Users currentUser = userService.findUserByGhanaCard("GHA-726682342-4");
                    userSession.loginUser(currentUser);

                    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                    JSF.addSuccessMessageWithSummary("Welcome Back! ", currentUser.getUsername());

                    //LOG USING AUDIT LOG
                    FacesContext.getCurrentInstance().getExternalContext().redirect("app/dashboard2.xhtml");
                    return null;

                }
                // Super admin credentials failed
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "LOGIN FAILED", "Invalid Credentials"));
                return null;

            }

            Users currentUser = userService.findUserByEmail(email);

            if (currentUser != null) {
                if (userService.checkPassword(password, currentUser.getPassword())) {
                    if (PendingPasswordChange(currentUser)) {
                        return null;
                    }

                    userSession.loginUser(currentUser);

                    if (userSession.userActive()) {

                        auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.SUCCESS, currentUser.getUsername() + " Login Succesful", currentUser);

                        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                        JSF.addSuccessMessageWithSummary("Welcome ! ", currentUser.getUsername());
                        FacesContext.getCurrentInstance().getExternalContext().redirect("app/dashboard2.xhtml");
                        return null;
                    } else {
                        auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.FAILED, currentUser.getUsername() + " Login Failed User not Active ", currentUser);
                        JSF.addErrorMessage("User InActive");
                        return null;
                    }
                } else {
                    auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.FAILED, currentUser.getUsername() + " Login Failed Incorrect Password ", currentUser);
                    JSF.addErrorMessage("Invalid Credentials");
                    return null;
                }
            } else {
                // User not found - log failed attempt
                auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.FAILED,
                        "Login failed - user not found for email: " + email, null);

                // Show error message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "LOGIN FAILED", "User does not exist"));
                return null;
            }

        } catch (IOException e) {
            // Log the exception
            auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.FAILED,
                    "Login failed with exception for Ghana Card: " + ghanaCardNumber + " - " + e.getMessage(), null);

            // Show error message to user
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "LOGIN ERROR", "System error occurred during login"));

            e.printStackTrace();
            return null;
        }
    }

    private boolean allowLogin() {
        return (email.equals("SuperAdmin1")) && password.equals("InshaAllah");
    }

    private boolean handleSuperAdmin() {
        if (allowLogin()) {
            int userCount = userService.findAllUsers().size();

            if (userCount < 1) {
                System.out.println("First time setup - creating super admin");
                createSuperAdmin();
            } else {
                System.out.println("Super admin login successful");
            }
            return true;
        }
        return false;
    }

    public boolean PendingPasswordChange(Users currentUser) {
        if (currentUser.getStatus() == UserStatus.PENDING_PASSWORD_CHANGE) {
            // Store user in session
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("userMustChangePassword", currentUser);

            auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.SUCCESS,
                    currentUser.getUsername() + " Login Successful - Password change required", currentUser);

            showPasswordChangeModal = true;
            return true;
        }
        return false;
    }

    public void cancelPasswordChange() throws IOException {
        // Clear session and logout
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().remove("userMustChangePassword");
        showPasswordChangeModal = false;
        newPassword = null;
        confirmPassword = null;

        // Logout user
        userSession.logout();
        JSF.addErrorMessage("Password change cancelled. Please login again.");
    }

    public void changePassword() {
        Users user = (Users) FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get("userMustChangePassword");

        if (user == null) {
            JSF.addErrorMessage("Session expired. Please login again.");
            return;
        }

        String validationMessage = ValidationUtil.validatePassword(newPassword);
        if (validationMessage != null) {
            JSF.addErrorMessage(validationMessage);
            return;
        }

        // Validate new passwords match
        if (!newPassword.equals(confirmPassword)) {
            JSF.addErrorMessage("Passwords do not match");
            return;
        }

        try {
            // Update password and status
            user.setPassword(User_Service.passwordEncoder.encode(newPassword));
            user.setStatus(UserStatus.ACTIVE);

            userService.updateUser(user);

            // Clear session and modal
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().remove("userMustChangePassword");

            showPasswordChangeModal = false;

            // Clear form fields
            newPassword = null;
            confirmPassword = null;

            auditLogService.logActivity(AuditActionType.UPDATE, "SubLogin Page", ActionResult.SUCCESS,
                    user.getUsername() + " Password changed successfully", user);
            auditLogService.logActivity(AuditActionType.LOGIN, "SubLogin Page", ActionResult.SUCCESS,
                    user.getUsername() + " Login Succesful After Password Change", user);

            JSF.addSuccessMessage("Password changed successfully!");

            // Redirect to dashboard
            FacesContext.getCurrentInstance().getExternalContext().redirect("sublogin.xhtml");
            JSF.addSuccessMessage("Login With New Credentials");

        } catch (Exception e) {
            JSF.addErrorMessage("Error changing password: " + e.getMessage());
            auditLogService.logActivity(AuditActionType.UPDATE, "Login Page", ActionResult.FAILED,
                    user.getUsername() + " Password change failed: " + e.getMessage(), user);
        }
    }

    public void createSuperAdmin() {
        try {
            dummyDataSeeder.createAdminUser();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error >>>>>>>>" + e.getMessage());
        }

    }

    // Logout method
    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public void loginWithBiometrics() {
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }

//    public String getUserInitials() {
//        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");
//
//        if (username == null || username.trim().isEmpty()) {
//            return "??"; // Return default initials if username is missing
//        }
//
//        // Trim and split the name
//        String[] nameParts = username.trim().split("\\s+"); // Split by one or more spaces
//        if (nameParts.length == 1) {
//            return nameParts[0].substring(0, 1).toUpperCase(); // Single name case
//        }
//
//        // Take first letter of first and last name
//        return nameParts[0].substring(0, 1).toUpperCase() + nameParts[nameParts.length - 1].substring(0, 1).toUpperCase();
//    }
}
