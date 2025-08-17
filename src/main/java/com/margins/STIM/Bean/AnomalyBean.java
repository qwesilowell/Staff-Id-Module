/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AnomalySeverity;
import com.margins.STIM.entity.enums.AnomalyStatus;
import com.margins.STIM.entity.enums.AnomalyType;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.AnomalyReport;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.service.AccessAnomalyService;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.User_Service;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.JSF;
import com.margins.STIM.util.ValidationUtil;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
@Named("anomalyBean")
@ViewScoped
public class AnomalyBean implements Serializable {

    @Inject
    private AccessAnomalyService anomalyService;

    @Inject
    private DeviceService deviceService;

    @Inject
    private EntrancesService entranceService;

    @Inject
    private User_Service userService;

    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;
    @Inject
    private ReportManager rm;

    private Long unattended;

    private List<AccessAnomaly> filteredAnomalies;
    private List<AccessAnomaly> allAnomalies;

    private AnomalySeverity selectedSeverity;
    private AnomalyStatus selectedStatus = AnomalyStatus.UNATTENDED;
    private String employeeFilter;
    private Integer deviceFilter;
    private Integer entranceFilter;
    private LocalDate dateFilter;

    private boolean loading = false;
    private AccessAnomaly selectedAnomaly;
    private Integer selectedHandler;
    private LocalDateTime lastUpdated;

    private List<Devices> availableDevices;
    private List<Entrances> availableEntrances;
    private List<Users> availableHandlers;

    @PostConstruct
    public void init() {
        loadUnattendedCount();
        loadData();
        updateLastUpdated();
    }

    public void loadUnattendedCount() {
        unattended = anomalyService.countByStatus(AnomalyStatus.UNATTENDED);
    }

    public String getUnattendedDisplayCount() {
        if (unattended > 99) {
            return "99+";
        }
        return String.valueOf(unattended);
    }

    public void loadData() {
        loading = true;
        try {
            loadUnattendedCount();
            loadDropdownData();
            loadFilteredAnomalies();
            updateLastUpdated();
        } finally {
            loading = false;
        }
    }

    private void updateLastUpdated() {
        lastUpdated = LocalDateTime.now();
    }

    private void loadDropdownData() {
        availableDevices = deviceService.getAllDevices();
        availableEntrances = entranceService.findAllEntrances();
        availableHandlers = userService.findAllUsers();
    }

    private void loadFilteredAnomalies() {
        filteredAnomalies = anomalyService.findWithFilters(
                selectedSeverity, selectedStatus, employeeFilter,
                deviceFilter, entranceFilter, dateFilter
        );
    }

    public void filterBySeverity(String severity) {
        selectedSeverity = severity != null ? AnomalySeverity.valueOf(severity) : null;
        loadFilteredAnomalies();
    }

    public void filterByStatus(String status) {
        selectedStatus = status != null ? AnomalyStatus.valueOf(status) : null;
        loadFilteredAnomalies();
    }

    public void applyFilters() {
        loadFilteredAnomalies();
    }

    public void clearFilters() {
        selectedSeverity = null;
        selectedStatus = AnomalyStatus.UNATTENDED;
        employeeFilter = null;
        deviceFilter = null;
        entranceFilter = null;
        dateFilter = null;
        loadFilteredAnomalies();
    }

    public void markAsPending(AccessAnomaly anomaly) {
        try {
            Users currentUser = userSession.getCurrentUser();
            anomalyService.markAsPending(anomaly.getId(), currentUser);
            loadData(); // Refresh data
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Anomaly marked as pending"));
            String auditDetail = currentUser.getUsername() + " marked the anomaly with ID: " + anomaly.getId() + " as pending.";
            auditLogService.logActivity(AuditActionType.UPDATE, "Anomaly Page", ActionResult.SUCCESS, auditDetail, currentUser);

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update anomaly"));
            String errorDetail = "Exception while marking anomaly with ID: " + anomaly.getId() + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.UPDATE, "Anomaly Page", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
        }
    }

    public void resolveAnomaly(AccessAnomaly anomaly) {
        try {
//            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
//            String username = (String) externalContext.getSessionMap().get("username");          
            Users currentUser = userSession.getCurrentUser();
            System.out.println("current User >>>>>>>>> " + currentUser);
            anomalyService.markAsResolved(anomaly.getId(), currentUser);
            loadData(); // Refresh data
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Anomaly resolved successfully"));

            String auditDetail = currentUser.getUsername() + " resolved the anomaly with ID: " + anomaly.getId() + ".";
            auditLogService.logActivity(AuditActionType.UPDATE, "Anomaly Page", ActionResult.SUCCESS, auditDetail, currentUser);
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to resolve anomaly"));
            String errorDetail = "Exception while resolving anomaly with ID: " + anomaly.getId() + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.UPDATE, "Anomaly Page", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
        }
    }

    public void showAssignDialog(AccessAnomaly anomaly) {
        selectedAnomaly = anomaly;
        selectedHandler = null;
    }

    public void navigateToAnomalyPage() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(externalContext.getRequestContextPath() + "/app/Anomalies/anomaliesPage.xhtml");
        } catch (IOException e) {
            // Handle exception appropriately
            System.err.println("Error navigating to anomaly page: " + e.getMessage());
        }
    }

    public void assignHandler() {
        if (selectedAnomaly != null && selectedHandler != null) {
            try {
                Users handler = availableHandlers.stream()
                        .filter(u -> u.getId() == selectedHandler)
                        .findFirst()
                        .orElse(null);

                if (handler != null) {
                    anomalyService.assignHandler(selectedAnomaly.getId(), handler);
                    loadData(); // Refresh data
                    String auditDetail = "Assigned anomaly with ID: " + selectedAnomaly.getId()+ " to "+ handler.getUsername() + ".";
                    auditLogService.logActivity(AuditActionType.UPDATE, "Anomaly Page", ActionResult.SUCCESS, auditDetail, userSession.getCurrentUser());

                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Handler assigned successfully"));
                }
            } catch (Exception e) {
                String errorDetail = "Exception while assigning handler to anomaly with ID: " + selectedAnomaly.getId() + ". Error: " + e.getMessage();
                auditLogService.logActivity(AuditActionType.UPDATE, "Anomaly Page", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to assign handler"));
            }
        }
    }

    public void showDetails(AccessAnomaly anomaly) {
        selectedAnomaly = anomaly;
    }

    public String formatAnomalyType(AnomalyType type) {
        return Arrays.stream(type.name().toLowerCase().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    // Severity counts for cards
    public Long getSevereCount() {
        return anomalyService.countBySeverityAndStatus(AnomalySeverity.SEVERE, selectedStatus);
    }

    public Long getWarningCount() {
        return anomalyService.countBySeverityAndStatus(AnomalySeverity.WARNING, selectedStatus);
    }

    public Long getInfoCount() {
        return anomalyService.countBySeverityAndStatus(AnomalySeverity.INFO, selectedStatus);
    }

    public String getFormattedAnomalyType(AnomalyType anomalyType) {
        return ValidationUtil.beautifyEnumName(anomalyType);
    }

    public String getLastUpdated() {
        if (lastUpdated != null) {
            return lastUpdated.format(DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm"));
        }
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm"));
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String formatTimestamp(AccessAnomaly anomaly) {
        if (anomaly != null && anomaly.getTimestamp() != null) {
            return DateFormatter.forDateTime(anomaly.getTimestamp());
        }
        return "N/A";
    }

    public void setupBreadcrumb() {
        if (breadcrumbBean != null) {
            breadcrumbBean.setSettingsPage();
        }
    }

    public String selectedDevice() {
        if (this.deviceFilter != null) {
            Devices device = deviceService.findDeviceById(deviceFilter);
            return device.getDeviceName();
        } else {
            return "N/A";
        }
    }

    public String selectedEntrance() {
        if (this.entranceFilter != null) {
            Entrances ent = entranceService.findEntranceById(entranceFilter);
            return ent.getEntranceName();
        } else {
            return "ALL ENTRANCES";
        }

    }

    public void breadcrumb() {
        if (breadcrumbBean != null) {
            breadcrumbBean.setAnomalyPage();
        } else {
            JSF.addWarningMessage("Breadcrumb Empty");
        }
    }

    private String safeValue(String value) {
        return (value != null && !value.isEmpty()) ? value : "N/A";
    }

    public void export() {
        List<AnomalyReport> ar = new ArrayList<>();

        for (AccessAnomaly a : filteredAnomalies) {
            AnomalyReport anomaly = new AnomalyReport();
            anomaly.setSeverityLevel(a.getAnomalySeverity().toString());
            anomaly.setAnomalyType(a.getAnomalyType().toString());
            anomaly.setAnomalyStatus(a.getAnomalyStatus().toString());
            anomaly.setEmpName(a.getEmployee().getFullName());
            anomaly.setIssue(a.getMessage());
            anomaly.setEntranceName(a.getEntrance().getEntranceName());
            anomaly.setDeviceName(a.getDevice().getDeviceName());
            anomaly.setTimestamp(formatTimestamp(a));

            ar.add(anomaly);

        }

        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.addParam("selectedSeverity", (selectedSeverity != null ? selectedSeverity.toString() : "ALL"));
        rm.addParam("empName", safeValue(employeeFilter));
        rm.addParam("deviceUsed", selectedDevice());
        rm.addParam("entranceSelected", selectedEntrance());
        rm.addParam("anomalyStatus", selectedStatus.toString());
        rm.addParam("date", (dateFilter != null ? DateFormatter.forLocalDate(dateFilter) : "N/A"));
        rm.addParam("anomalyCount", filteredAnomalies.size());
        rm.addParam("severeCount", getSevereCount());
        rm.addParam("infoCount", getInfoCount());
        rm.addParam("warningCount", getWarningCount());

        rm.addParam("anomalyData", new JRBeanCollectionDataSource(ar));
        rm.setReportFile(ReportGenerator.ANOMALY_REPORT);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);
    }

    public void print() {
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.addParam("severityLevel", selectedAnomaly.getAnomalySeverity().toString());
        rm.addParam("anomalyType", selectedAnomaly.getAnomalyType().toString());
        rm.addParam("empName", selectedAnomaly.getEmployee().getFullName());
        rm.addParam("entranceName", selectedAnomaly.getEntrance().getEntranceName());
        rm.addParam("deviceName", selectedAnomaly.getDevice().getDeviceName());
        rm.addParam("issueDetails", selectedAnomaly.getMessage());
        rm.addParam("timestamp", formatTimestamp(selectedAnomaly));
        rm.addParam("handlerName", (selectedAnomaly.getHandledBy() != null ? selectedAnomaly.getHandledBy().getUsername() : "UNASSIGNED"));
        rm.addParam("status", selectedAnomaly.getAnomalyStatus().toString());

        rm.setReportFile(ReportGenerator.ANOMALY_DETAILS);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);
        rm.setFilename("Anomaly_Details_Report");
    }
}
