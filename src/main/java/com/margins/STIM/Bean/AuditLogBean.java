/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.AuditLog;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.AuditLogReport;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.margins.STIM.lazyloading.LazyAuditlog;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.primefaces.model.LazyDataModel;

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

    @Inject
    private ReportManager rm;

    @Inject
    private UserSession userSession;

    private List<AuditLog> allLogs;

    private LazyDataModel<AuditLog> allLazyAuditLogs;

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
//        allLogs = auditLogService.filterAuditLogs(timeRange, selectedAction, selectedResult, selectedUser, details);
        allLazyAuditLogs = new LazyAuditlog(auditLogService, details, timeRange, selectedResult, selectedUser, selectedAction);
    }

    public void getLogs() {
//        allLogs = auditLogService.getAllActivities();
        allLazyAuditLogs = new LazyAuditlog(auditLogService, null, null, null, null, null);

    }

    public List<Users> completeUsers(String query) {
        if (allUsers == null) {
            allUsers = userService.findAllUsers();
        }
        if (query == null || query.trim().isEmpty()) {
            return allUsers;
        }

        String searchQuery = query.toLowerCase().trim();
        List<Users> filtered = allUsers.stream()
                .filter(users
                        -> users.getUsername().toLowerCase().contains(searchQuery)
                || (users.getUsername() + "-" + users.getUserType()).toLowerCase().contains(searchQuery)
                )
                .collect(Collectors.toList());
        return filtered;
    }

    public String getFormatTime(AuditLog log) {
        if (log != null && log.getCreatedOn() != null) {
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

    private String getTimeRangeAsString() {
        if (timeRange != null && timeRange.size() == 2) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return timeRange.get(0).format(formatter) + " to " + timeRange.get(1).format(formatter);
        }
        return "N/A";
    }

    public void export() {
        List<AuditLogReport> ar = new ArrayList<>();

        for (AuditLog a : allLazyAuditLogs) {
            AuditLogReport audit = new AuditLogReport();
            audit.setId(a.getId());
            audit.setAction(a.getAction().toString());
            audit.setDetails(a.getDetails());
            audit.setPage(a.getEntityName());
            audit.setPerformedBy((a.getPerformedBy() != null ? a.getPerformedBy().getUsername() : "SYSTEM"));
            audit.setResult(a.getResult().toString());
            audit.setTimestamp(getFormatTime(a));

            ar.add(audit);
        }
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.addParam("user", (selectedUser != null ? selectedUser.getUsername() : "N/A"));
        rm.addParam("actionType", (selectedAction != null ? selectedAction.toString() : "N/A"));
        rm.addParam("result", (selectedResult != null ? selectedResult.toString() : "N/A"));
        rm.addParam("date", getTimeRangeAsString());
        rm.addParam("auditData", new JRBeanCollectionDataSource(ar));
        rm.setReportFile(ReportGenerator.AUDIT_LOG);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);
    }
}
