/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.AuditLog;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Named
@ViewScoped
public class AuditLogBean implements Serializable {

    @Inject
    private AuditLogService auditLogService;

    @Inject
    User_Service userService;
    
    @Inject
    private BreadcrumbBean breadcrumbBean;

    private List<AuditLog> allLogs;

    private Users selectedUser;
    private AuditActionType selectedAction;
    private ActionResult selectedResult;
    private List<LocalDateTime> timeRange;
    private List<Users> allUsers;
    private String details;

    private List<AuditActionType> actionTypes = Arrays.asList(AuditActionType.values());
    private List<ActionResult> resultTypes = Arrays.asList(ActionResult.values());

    @PostConstruct
    public void init() {
        allUsers = userService.findAllUsers();
        getLogs();

    }

    public void filterLogs() {
        allLogs = auditLogService.filterAuditLogs(timeRange, selectedAction, selectedResult, selectedUser, details);
    }

    public void getLogs() {
        allLogs = auditLogService.getAllActivities();
    }

    public List<Users> completeUsers(String query) {
        if (allUsers == null) {
            allUsers = userService.findAllUsers();
        }
        if (query == null || query.trim().isEmpty()) {
            return allUsers;
        }
        List<Users> filtered = allUsers.stream()
                .filter(users -> users.getUsername().toLowerCase()
                .contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return filtered;

    }
    
    public String getFormatTime(AuditLog log){
        if (log != null && log.getCreatedOn()!= null) {
            return DateFormatter.forDateTime(log.getCreatedOn());
        }
         return "N/A";
    }

    public void reset() {
        timeRange = null;
        selectedAction = null;
        selectedResult = null;
        selectedUser = null;
        details = null;
        getLogs();
    }
    
    public void setupBreadcrumb() {
        breadcrumbBean.setActivityLogPage();
    }
}
