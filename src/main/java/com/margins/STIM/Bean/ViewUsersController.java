/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.SystemUserRoles;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.enums.UserStatus;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.UserReport;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.UserRolesServices;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.EmailSender;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Named("viewUsers")
@ViewScoped
public class ViewUsersController implements Serializable {
    
    @Inject
    private User_Service userService;
    
    @Inject
    private UserRolesServices userRolesService;
    
    @Inject
    private AuditLogService auditLogService;
    
    @Inject
    private BreadcrumbBean breadcrumbBean;
    
    @Inject
    private ReportManager rm;
    
    @Inject
    UserSession userSession;
    
    private List<Users> allUsers;
    private String filterType = "ALL";
    private List<Users> filteredUsers;
    private List<SystemUserRoles> allSystemRoles;
    private Users selectedUser;
    private Set<ViewPermission> selectedUserPermissions;
    
    public void setupBreadcrumb() {
        breadcrumbBean.setUserManagementPage();
    }
    
    @PostConstruct
    public void init() {
        try {
            allUsers = userService.findAllUsers();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error loading users", e.getMessage()));
        }
        
        allSystemRoles = userRolesService.getAllUserRoles();
        applyFilter(filterType);
    }
    
    public void updateUserRole() {
        try {
            
            userService.updateUser(selectedUser);

            // Log the role change
            Users currentUser = userService.getCurrentUser(); // Your existing method
            auditLogService.logActivity(AuditActionType.UPDATE, "User Management Page",
                    ActionResult.SUCCESS,
                    "Updated role for user " + selectedUser.getUsername() + " to " + selectedUser.getUserType(),
                    currentUser);
            
            applyFilter(filterType);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "User role updated successfully"));
            
        } catch (Exception e) {
            Users currentUser = userService.getCurrentUser();
            auditLogService.logActivity(AuditActionType.UPDATE, "User Management Page",
                    ActionResult.FAILED,
                    "Failed to update role for user " + selectedUser.getUsername() + ": " + e.getMessage(),
                    currentUser);
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "Failed to update user role: " + e.getMessage()));
        }
    }
    
    public void deleteUser(Users user) {
        if (user == null) {
            JSF.addErrorMessage("No user selected for deletion.");
            return;
        }
        
        this.selectedUser = user;
        userService.deleteUser(selectedUser);
        applyFilter(filterType);
        
        if (allUsers != null) {
            allUsers.remove(user);
        }
        if (filteredUsers != null) {
            filteredUsers.remove(user);
        }
        
        JSF.addSuccessMessage("User " + selectedUser.getUsername() + " deleted successfully.");
        this.selectedUser = null;
    }
    
    public int getTotalUsers() {
        return allUsers.size();
    }
    
    public int getActiveUsers() {
        return (int) allUsers.stream()
                .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                .count();
    }
    
    public int getInactiveUsers() {
        return (int) allUsers.stream()
                .filter(u -> UserStatus.INACTIVE.equals(u.getStatus()))
                .count();
    }

    // Filter methods
    public void applyFilter(String filterType) {
        this.filterType = filterType; // Update the filterType property
        switch (filterType) {
            case "ACTIVE":
                filteredUsers = allUsers.stream()
                        .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))
                        .collect(Collectors.toList());
                break;
            case "INACTIVE":
                filteredUsers = allUsers.stream()
                        .filter(u -> UserStatus.INACTIVE.equals(u.getStatus()))
                        .collect(Collectors.toList());
                break;
            default:
                filteredUsers = new ArrayList<>(allUsers);
        }
        if (filteredUsers.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "No Users",
                            "No " + filterType.toLowerCase() + " users found in the system."));
        }
    }
    
    public void openEditDialog(Users user) {
        System.out.println("Edit button Selected>>>>>>> " + user.getUsername());
        this.selectedUser = user;
    }
    
    public String getUserInitials(Users user) {
        String username = user.getUsername();
        if (username == null || username.trim().isEmpty()) {
            return "??";
        }

        // Split by one or more spaces
        String[] nameParts = username.trim().split("\\s+");
        
        if (nameParts.length == 1) {
            return getFirstChar(nameParts[0]);
        }

        // First name + last name (or last word)
        return getFirstChar(nameParts[0]) + getFirstChar(nameParts[nameParts.length - 1]);
    }
    
    private String getFirstChar(String word) {
        return (word != null && word.length() >= 1) ? word.substring(0, 1).toUpperCase() : "?";
    }

    // Status toggle
    public void toggleUserStatus(Users user) {
        try {
            UserStatus newStatus = UserStatus.ACTIVE.equals(user.getStatus())
                    ? UserStatus.INACTIVE
                    : UserStatus.ACTIVE;
            user.setStatus(newStatus);
            userService.updateUser(user);
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Success", "User status updated to " + newStatus.name()));
            
            applyFilter(filterType); // Refresh filtered list
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error", "Failed to update user status"));
        }
    }
    
    public void viewUserPermissions(Users user) {
        selectedUser = user;
        if (user.getUserRole() != null) {
            selectedUserPermissions = user.getUserRole().getPermissions();
        } else {
            selectedUserPermissions = new HashSet<>();
        }
    }
    
    public void export() {
        
        List<UserReport> ur = new ArrayList<>();
        
        for (Users u : filteredUsers) {
            UserReport userReport = new UserReport(u, u.getUserRole().getPermissions());
            ur.add(userReport);
        }
        
        rm.addParam("printedBy", userSession.getUsername());
        rm.addParam("userData", new JRBeanCollectionDataSource(ur));
        rm.setReportFile(ReportGenerator.USER_REPORT);
        rm.setReportDataList(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);
        
    }
    
//    public void testEmailSender() {
//        if (EmailSender.sendEmail()) {
//            JSF.addSuccessMessage("Message sent Succesfully");
//        } else {
//            JSF.addErrorMessage("Message Failed to Send");
//        }
//    }
}
