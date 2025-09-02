/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.ReportsService;
import com.margins.STIM.DTO.EntranceReportDTO;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.DeviceData;
import com.margins.STIM.report.model.EntranceInformation;
import com.margins.STIM.report.model.EntranceReport;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.primefaces.PrimeFaces;
import org.primefaces.model.charts.line.LineChartModel;

/**
 *
 * @author PhilipManteAsare
 */
@Named("entranceReportBean")
@ViewScoped
@Getter
@Setter
public class EntranceReportController implements Serializable {

    private static final Logger LOGGER = Logger.getLogger(EntranceReportController.class.getName());

    @EJB
    private ReportsService entranceReportService;

    @EJB
    private EntrancesService entranceService;

    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private UserSession userSession;

    @Inject
    private ReportManager rm;

    // Filter properties
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int selectedEntranceId;

    // Data properties
    private List<EntranceReportDTO> reportData;
    private List<Entrances> availableEntrances;
    private EntranceReportDTO selectedEntranceDetail;
    private Entrances selectedEntrance;
    private List<Employee> selectedEntEmployee = new ArrayList<>();
    private Set<EmployeeRole> selectedEntRoles = new HashSet<>();
    private List<Devices> selectedDevices = new ArrayList<>();

// Chart properties not used
    private LineChartModel chartModel;

    @PostConstruct
    public void init() {
        // Load available entrances for filter dropdown
        availableEntrances = entranceService.findAllEntrances();

        // Load initial data (all entrances, no date filter)
        loadReport();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setEntrancesReportBreadcrumb();
    }

    public void loadReport() {
        try {
            // Validate date range
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                addErrorMessage("Start date must be before end date");
                return;
            }

            // Load report data
            reportData = entranceReportService.getEntranceReport(startDate, endDate, selectedEntranceId);

            if (reportData.isEmpty()) {
                addInfoMessage("No data found for the selected criteria");
            } else {
                addInfoMessage("Report loaded successfully ");
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading entrance report", e);
            addErrorMessage("Error loading report data: " + e.getMessage());
        }
    }

    public void clearFilters() {
        startDate = null;
        endDate = null;
//        selectedEntranceId = null;
        loadReport();
        addInfoMessage("Filters cleared");
    }

    public void viewDetails(int entranceId) {
        try {
            // Find the selected entrance detail
            selectedEntranceDetail = reportData.stream()
                    .filter(report -> report.getEntrances().getId() == (entranceId))
                    .findFirst()
                    .orElse(null);
            selectedEntrance = entranceService.findEntranceById(entranceId);

            if (selectedEntranceDetail == null) {
                System.out.println("Entrance details not found");
            }

            if (selectedEntranceDetail != null) {
                System.out.println("selectedEntranceDetail>>>>>>>>>> " + selectedEntranceDetail);
                addInfoMessage("Data found render!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading entrance details: " + entranceId, e);
            System.out.println("Error loading entrance details");
        }
    }

    public void viewEmployees(int entranceId) {
        selectedEntrance = entranceService.findEntranceById(entranceId);
        loadEmployeesForEntrance();
        loadRolesForEnt();
    }

    public void loadEmployeesForEntrance() {
        selectedEntEmployee.clear();
        if (selectedEntrance != null && selectedEntrance.getEmployees() != null) {
            List<Employee> employees = selectedEntrance.getEmployees();

            if (employees != null) {
                selectedEntEmployee.addAll(employees);
            }
        } else {
            addErrorMessage("No employees assigned to this entrance");
        }
    }

    public void loadRolesForEnt() {
        selectedEntRoles.clear();

        if (selectedEntrance != null && selectedEntrance.getAllowedRoles() != null) {
            Set<EmployeeRole> roles = selectedEntrance.getAllowedRoles();

            if (roles != null) {
                selectedEntRoles.addAll(roles);
            }
        } else {
            addErrorMessage("No roles assigned to this entrance");
        }

    }

    public void viewDevice(List<Devices> device, Entrances entrance) {
        selectedEntrance = entrance;
        if (device != null && !device.isEmpty()) {
            selectedDevices = new ArrayList<>(device);
        } else {
            selectedDevices = Collections.emptyList();
            addErrorMessage("No devices assigned to this entrance");
        }

    }

    public String getAssignedDeviceLabel() {
        if (selectedDevices == null || selectedDevices.isEmpty()) {
            return "NO DEVICES ASSIGNED TO ENTRANCE";
        }

        boolean hasEntry = selectedDevices.stream()
                .anyMatch(d -> d.getDevicePosition() == DevicePosition.ENTRY);
        boolean hasExit = selectedDevices.stream()
                .anyMatch(d -> d.getDevicePosition() == DevicePosition.EXIT);

        if (hasEntry && !hasExit) {
            return "ENTRY DEVICES ASSIGNED TO ENTRANCE";
        } else if (!hasEntry && hasExit) {
            return "EXIT DEVICES ASSIGNED TO ENTRANCE";
        } else if (hasEntry && hasExit) {
            return "ENTRY & EXIT DEVICES ASSIGNED TO ENTRANCE";
        }

        return "NO DEVICES ASSIGNED TO ENTRANCE";
    }

    public String navigateToHistory(int entranceId) {
        if (0 == entranceId) {
            addErrorMessage("No entrance selcted");
            return null;
        } else {
            selectedEntrance = entranceService.findEntranceById(entranceId);

            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying All Access for " + selectedEntrance.getEntranceName()));
            PrimeFaces.current().ajax().update("entForm");

            // Return the outcome with the parameter for filtering
            return "/app/Access/historyMonitoring?faces-redirect=true&entranceId=" + entranceId;
        }
    }

    public String navToResult(int entranceId, String result) {
        selectedEntrance = entranceService.findEntranceById(entranceId);

        String resultText;
        if ("granted".equals(result)) {
            resultText = "Granted";
        } else {
            resultText = "Denied";
        }

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying " + resultText + " Access for " + selectedEntrance.getEntranceName()));
        PrimeFaces.current().ajax().update("entForm");

        // Return the outcome with the parameter for filtering
        return "/app/Access/historyMonitoring?faces-redirect=true&entranceId=" + entranceId + "&result=" + result;
    }

    // Summary calculations
    public int getTotalEntrances() {
        return availableEntrances != null ? availableEntrances.size() : 0;
    }

    public int getActiveEntrances() {
        return reportData != null ? reportData.size() : 0;
    }

    public long getTotalAccessAttempts() {
        return reportData != null
                ? reportData.stream().mapToLong(EntranceReportDTO::getTotalAccesses).sum() : 0;
    }

    public long getTotalGrantedAccess() {
        if (reportData == null) {
            return 0L;
        }
        return reportData.stream()
                .mapToLong(dto -> dto.getGrantedAccesses())
                .sum();
    }

    public long getTotalDeniedAccess() {
        if (reportData == null) {
            return 0L;
        }
        return reportData.stream()
                .mapToLong(dto -> dto.getDeniedAccesses())
                .sum();

    }

    public String formatTimeString(LocalDateTime dt) {
        return DateFormatter.forDateTimes(dt);
    }

    // Utility methods
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    public void exportPage(String fileType) {
        List<EntranceReport> er = new ArrayList<>();

        for (EntranceReportDTO e : reportData) {
            EntranceReport entReport = new EntranceReport();

            entReport.setEntranceName(e.getEntrances().getEntranceName());
            entReport.setEntranceLocation(e.getEntrances().getEntranceLocation());
            entReport.setTotalAccesses(e.getTotalAccesses());
            entReport.setGrantedAccesses(e.getGrantedAccesses());
            entReport.setDeniedAccesses(e.getDeniedAccesses());
            entReport.setLastAccessedBy(e.getLastAccessedBy());
            entReport.setLastAccessStatus(e.getLastAccessStatus());
            String lastAccessAt = DateFormatter.forDateTime(e.getLastAccessed());
            entReport.setLastAccessed(lastAccessAt);
            entReport.setRoleCount(e.getRoleCount());
            entReport.setEmployeeCount(e.getEmployeeCount());

            er.add(entReport);
        }

        ReportOutputFileType type = ReportOutputFileType.valueOf(fileType.toUpperCase());
        rm.addParam("totalEntrances", getTotalEntrances());
        rm.addParam("activeEntrances", getActiveEntrances());
        rm.addParam("totalAttempts", getTotalAccessAttempts());
        rm.addParam("grantedAccess", getTotalGrantedAccess());
        rm.addParam("deniedAccess", getTotalDeniedAccess());
        rm.addParam("printedBy", userSession.getUsername());
        rm.addParam("entranceReportTable", new JRBeanCollectionDataSource(er));
        rm.setReportFile(ReportGenerator.ENTRANCES_REPORT);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(type);
    }

    public void exportEntranceInfo() {
        List<EntranceInformation> employeeList = new ArrayList<>();

        for (Employee e : selectedEntranceDetail.getEntrances().getEmployees()) {
            EntranceInformation eInfo = new EntranceInformation();
            eInfo.setEmpName(e.getFullName());
            eInfo.setEmpEmail(e.getEmail());
            eInfo.setEmpGhanaCard(e.getGhanaCardNumber());
            eInfo.setEmpRole(e.getRole().getRoleName());
            eInfo.setEmpPhone(e.getPrimaryPhone());

            employeeList.add(eInfo);
        }

        List<DeviceData> devData = new ArrayList<>();

        // Process entry devices
        if (selectedEntranceDetail.getEntrydevice() != null) {
            for (Devices device : selectedEntranceDetail.getEntrydevice()) {
                DeviceData eInfo = new DeviceData();
                eInfo.setDeviceId(device.getDeviceId());
                eInfo.setDeviceName(device.getDeviceName());
                eInfo.setDevicePosition(device.getDevicePosition().toString());
                devData.add(eInfo);
            }
        }

        // Process exit devices
        if (selectedEntranceDetail.getExitdevice() != null) {
            for (Devices device : selectedEntranceDetail.getExitdevice()) {
                DeviceData eInfo = new DeviceData();
                eInfo.setDeviceId(device.getDeviceId());
                eInfo.setDeviceName(device.getDeviceName());
                eInfo.setDevicePosition(device.getDevicePosition().toString());
                devData.add(eInfo);
            }
        }

        Set<EmployeeRole> roleData = new HashSet<>();

        for (EmployeeRole e : selectedEntranceDetail.getEntrances().getAllowedRoles()) {
            EmployeeRole er = new EmployeeRole();
            er.setRoleName(e.getRoleName());

            roleData.add(er);
        }

        rm.addParam("entranceName", selectedEntranceDetail.getEntrances().getEntranceName());
        rm.addParam("printedBy", userSession.getUsername());
        rm.addParam("entranceId", selectedEntranceDetail.getEntrances().getEntranceId());
        rm.addParam("entranceLocation", selectedEntranceDetail.getEntrances().getEntranceLocation());

        rm.addParam("entranceMode", selectedEntranceDetail.getEntrances().getEntranceMode().toString());
        rm.addParam("totalAccess", selectedEntranceDetail.getTotalAccesses());
        rm.addParam("grantedAccess", selectedEntranceDetail.getGrantedAccesses());
        rm.addParam("deniedAccess", selectedEntranceDetail.getDeniedAccesses());
        String date = DateFormatter.formatDate(selectedEntranceDetail.getEntrances().getCreatedAt());
        rm.addParam("dateCreated", date);
        rm.addParam("lastAccessedBy", selectedEntranceDetail.getLastAccessedBy());
        String lastAccessAt = DateFormatter.forDateTime(selectedEntranceDetail.getLastAccessed());
        rm.addParam("timeLastAccessed", lastAccessAt);
        rm.addParam("status", selectedEntranceDetail.getLastAccessStatus());
        rm.addParam("deviceUsed", selectedEntranceDetail.getDeviceUsed());
        rm.addParam("employeeData", new JRBeanCollectionDataSource(employeeList));
        System.out.println("list count >>>>>>>>>>" + employeeList.size());
        rm.addParam("devicesData", new JRBeanCollectionDataSource(devData));
        rm.addParam("roleData", new JRBeanCollectionDataSource(roleData));
        rm.setReportFile(ReportGenerator.ENTRANCES_INFORMATION);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);

    }
}
