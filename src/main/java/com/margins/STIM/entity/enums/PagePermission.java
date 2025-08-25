package com.margins.STIM.entity.enums;


import lombok.Getter;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author PhilipManteAsare
 */
@Getter
public enum PagePermission {

    SETTINGS("/app/Settings/settingsPage.xhtml", "Settings"),
    CREATE_NEW_USER("/app/Settings/newuser.xhtml", "Create New System User"),
    VIEW_USERS("/app/Settings/viewUsers.xhtml", "View All System Users"),
    MANAGE_USER_ROLES("/app/Settings/createUserRoles.xhtml", "Manage User Roles"),
    ANOMALY_MANAGEMENT("/app/Anomalies/anomaliesPage.xhtml", "Anomaly Management"),
    
    ACTIVITY_LOG("/app/Settings/activityLog.xhtml", "View All activites"),
    DEVELOPER_TOOLS("/app/Settings/developerTools.xhtml", "For Developer"),

    DASHBOARD("/app/dashboard2.xhtml", "Dashboard"),
    
    // Employee Management
    ONBOARD_EMPLOYEE("/app/OnboardEmployee/onboardEmployee.xhtml", "Onboard Employee"),
    EMPLOYEE_PROFILES("/app/employeeList.xhtml", "Employee Profiles"),
    UPDATE_EMPLOYEE_DETAILS("/app/Verification/singleFinger.xhtml", "Update Employee Details"),
    
    // Employee Roles
    UPDATE_EMPLOYEE_ROLE("/app/EmployeeRoles/assignRole.xhtml", "Update Employee Role"),
    CREATE_EMPLOYEE_ROLE("/app/EmployeeRoles/createRole.xhtml", "Create Employee Role"),
    ADD_EMPLOYEE_STATUS("/app/OnboardEmployee/empStatusPage.xhtml", "Add Employee Status"),
    
    // Access Assignment
    ASSIGN_ROLES_TO_ENTRANCES("/app/Access/assign-roles.xhtml", "Assign Roles to Entrances"),
    ASSIGN_CUSTOM_ENTRANCE("/app/Access/assignEntrance.xhtml", "Assign Custom Entrance"),
    
    // Entrances & Devices
    MANAGE_ENTRANCES("/app/Entrances/manageEntrances.xhtml", "Manage Entrances"),
    MANAGE_ENTRANCE_DEVICES("/app/Entrances/manageDevices.xhtml", "Manage Entrance Devices"),
    STRICT_MODE_RESET("/app/Entrances/resetEmployeeEntranceState.xhtml", "Strict Mode Reset"),
    
    // Entrance Access Test
    ENTRANCE_ACCESS_TEST("/app/Entrances/accessControl.xhtml", "Entrance Access Test"),
    
    // Monitoring and Reports
    REAL_TIME_MONITORING("/app/Access/liveMonitoring.xhtml", "Real-Time Monitoring"),
    HISTORY_MONITORING("/app/Access/historyMonitoring.xhtml", "History Monitoring"),
    REPORTS("/app/Reports/Reports.xhtml", "Reports Page"),
    EMPLOYEE_REPORTS("/app/Reports/employeeReport.xhtml", "Employee Reports"),
    ENTRANCE_ACCESS_REPORTS("/app/Reports/entranceAccessReport.xhtml", "Entrance Access Reports"),
    ROLE_REPORTS("/app/Reports/roleReport.xhtml", "Role Reports"),
    ENTRANCE_REPORTS("/app/Reports/entrancesReport.xhtml", "Entrance Reports")
    
    
    
    ;
      
    private final String pagePath;
    private final String displayName;

    PagePermission(String pagePath, String displayName) {
        this.pagePath = pagePath;
        this.displayName = displayName;
    }

}
