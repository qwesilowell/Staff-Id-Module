/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author PhilipManteAsare
 */
@Named("breadcrumbBean")
@SessionScoped
public class BreadcrumbBean implements Serializable {

    @Getter
    @Setter
    private MenuModel model;

    String contextPath;

    @PostConstruct
    public void init() {
        model = new DefaultMenuModel();
        contextPath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
        addBreadcrumb("Dashboard", contextPath + "/app/dashboard2.xhtml");
    }

    public void addBreadcrumb(String label, String url) {
        DefaultMenuItem item = new DefaultMenuItem();
        item.setValue(label);
        item.setUrl(url);
        model.getElements().add(item);

    }

    public void addBreadcrumb(String label, String url, boolean disabled) {
        DefaultMenuItem item = new DefaultMenuItem();
        item.setValue(label);
        item.setUrl(url);
        item.setDisabled(disabled);
        model.getElements().add(item);
    }

    public void clearBreadcrumbs() {
        model = new DefaultMenuModel();
    }

    public void resetToDashboard() {
        clearBreadcrumbs();
        addBreadcrumb("Dashboard", contextPath + "/app/dashboard2.xhtml");
    }

    public void setEmployeeListBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Employee Profiles", null, true);
    }

    public void setOnboardEmployeeBreadCrumb() {
        resetToDashboard();
        addBreadcrumb("Onboard Employee", null, true);
    }

    public void setUpdateEmployeeBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Update Employee Details", null, true);
    }

    // Employee Roles Section
    public void setAssignRoleBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Employee Roles", null);
        addBreadcrumb("Update Employee Role", null, true);
    }

    public void setCreateRoleBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Employee Roles", null);
        addBreadcrumb("Create Employee Role", null, true);
    }

    public void setEmployeeStatusBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Employee Roles", null);
        addBreadcrumb("Add Employee Status", null, true);
    }

    // Access Assignment Section
    public void setAssignRolesToEntrancesBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Assign Access", null);
        addBreadcrumb("Assign Roles to Entrances", null, true);
    }

    public void setAssignCustomEntranceBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Assign Access", null);
        addBreadcrumb("Assign Custom Entrance", null, true);
    }

    // Entrances Section
    public void setManageEntrancesBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Entrances", null);
        addBreadcrumb("Manage Entrances", null, true);
    }
    
    public void setManageEntrancesDevices() {
        resetToDashboard();
        addBreadcrumb("Entrances", null);
        addBreadcrumb("Manage Entrance Devices", null, true);
    }

    public void setAccessControlBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Entrance Test", null);
        addBreadcrumb("Entrance Access Test", null, true);
    }
    
    public void setEntranceStateBreadCrumb() {
        resetToDashboard();
        addBreadcrumb("Entrances", null);
        addBreadcrumb("Manage Employee States", null, true);
    }

    // Users Section
    public void setCreateNewUserBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Settings", contextPath + "/app/Settings/settingsPage.xhtml?faces-redirect=true");
        addBreadcrumb("Create Users", null, true);
    }

    // Monitoring and Reports Section
    public void setLiveMonitoringBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", null);
        addBreadcrumb("Real Time Monitoring", null, true);
    }

    public void setHistoryMonitoringBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", null);
        addBreadcrumb("History Monitoring", null, true);
    }

    public void setReportsBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", null);
        addBreadcrumb("Reports", null, true);
    }

    // Specific Report Types
    public void setEmployeeReportBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", contextPath + "/app/Reports/Reports.xhtml?faces-redirect=true");
        addBreadcrumb("Reports", contextPath + "/app/Reports/Reports.xhtml?faces-redirect=true");
        addBreadcrumb("Employees Report", null, true);
    }

    public void setRolesReportBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", contextPath + "/app/Reports/Reports.xhtml");
        addBreadcrumb("Reports", contextPath + "/app/Reports/Reports.xhtml");
        addBreadcrumb("Roles Report", null, true);
    }

    public void setEntrancesReportBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", contextPath + "/app/Reports/Reports?faces-redirect=true");
        addBreadcrumb("Reports", contextPath + "/app/Reports/Reports?faces-redirect=true");
        addBreadcrumb("Entrances Report", null, true);
    }

    public void setLoginsReportBreadcrumb() {
        resetToDashboard();
        addBreadcrumb("Monitoring and Reports", contextPath + "/app/Reports/Reports?faces-redirect=true");
        addBreadcrumb("Reports", contextPath + "/app/Reports/Reports?faces-redirect=true");
        addBreadcrumb("Logins Report", null, true);
    }

    //Settings Page
    public void setSettingsPage() {
        resetToDashboard();
        addBreadcrumb("Settings", null,true);
    }
    
    public void setUserManagementPage() {
        resetToDashboard();
        addBreadcrumb("Settings", contextPath + "/app/Settings/settingsPage.xhtml?faces-redirect=true");
        addBreadcrumb("User Management", null, true);
    }
    
    public void setActivityLogPage() {
        resetToDashboard();
        addBreadcrumb("Settings", contextPath + "/app/Settings/settingsPage.xhtml?faces-redirect=true");
        addBreadcrumb("Activity Log", null, true);
    }
    
    public void setAnomalyInformationPage() {
        resetToDashboard();
        addBreadcrumb("Settings", contextPath + "/app/Settings/settingsPage.xhtml?faces-redirect=true");
        addBreadcrumb("Anomaly Information", null, true);
    }
    
    public void setDeveloperTools() {
        resetToDashboard();
        addBreadcrumb("Settings", contextPath + "/app/Settings/settingsPage.xhtml?faces-redirect=true");
        addBreadcrumb("Developer Tools", null, true);
    }
    
    public void setCreateUserRole() {
        resetToDashboard();
        addBreadcrumb("Settings", contextPath + "/app/Settings/settingsPage.xhtml?faces-redirect=true");
        addBreadcrumb("User Role and Page Management", null, true);
    }
    
    
    
     //Anomaly BreadCrumb
    public void setAnomalyPage() {
        resetToDashboard();
        addBreadcrumb("Anomaly page", null, true);
    }
}
