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
import com.margins.STIM.util.EntranceReportDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Getter;
import lombok.Setter;
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

    // Filter properties
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int selectedEntranceId;

    // Data properties
    private List<EntranceReportDTO> reportData;
    private List<Entrances> availableEntrances;
    private EntranceReportDTO selectedEntranceDetail;
    private Entrances selectedEntrance;
    private List <Employee> selectedEntEmployee = new ArrayList<>();
    private Set <EmployeeRole> selectedEntRoles = new HashSet<>();
    
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
        breadcrumbBean.setEmployeeReportBreadcrumb();
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

//    public void viewChart(String entranceId) {
//        try {
//            // Create chart for specific entrance
//            createAccessChart(entranceId);
//        } catch (Exception e) {
//            LOGGER.log(Level.SEVERE, "Error creating chart for entrance: " + entranceId, e);
//            addErrorMessage("Error creating chart");
//        }
//    }
    
    
    public void viewDetails(String entranceId) {
        try {
            // Find the selected entrance detail
            selectedEntranceDetail = reportData.stream()
                    .filter(report -> report.getEntrances().getEntranceDeviceId().equals(entranceId))
                    .findFirst()
                    .orElse(null);

            if (selectedEntranceDetail == null) {
                addErrorMessage("Entrance details not found");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading entrance details: " + entranceId, e);
            addErrorMessage("Error loading entrance details");
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
        }else {
            addErrorMessage("No employees assigned to this entrance");
        }
    }
    
    public void loadRolesForEnt(){
    selectedEntRoles.clear();
    
    if (selectedEntrance != null && selectedEntrance.getAllowedRoles() != null){
        Set <EmployeeRole> roles = selectedEntrance.getAllowedRoles();
    
        if(roles != null){
        selectedEntRoles.addAll(roles);
        }
    }else {
        addErrorMessage("No roles assigned to this entrance");
    }
    
    }

    public String navigateToHistory(int entranceId) {
        if(0  == entranceId){
            addErrorMessage("No entrance selcted");
            return null ;
        }
        else{
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

//    private void createAccessChart(String entranceId) {
//        chartModel = new LineChartModel();
//        ChartData data = new ChartData();
//
//        // Create dataset for access trends (this is a simplified example)
//        LineChartDataSet dataSet = new LineChartDataSet();
//        dataSet.setLabel("Access Attempts");
//        dataSet.setFill(false);
//        dataSet.setBorderColor("rgb(75, 192, 192)");
//        dataSet.setTension(0.1);
//
//        // Find the entrance data
//        EntranceReportDTO entranceData = reportData.stream()
//                .filter(report -> report.getEntrances().getEntranceDeviceId().equals(entranceId))
//                .findFirst()
//                .orElse(null);
//
//        if (entranceData != null) {
//            List<Object> values = new ArrayList<>();
//            List<String> labels = new ArrayList<>();
//
//            // Simple example - you might want to get actual time-series data
//            values.add(entranceData.getTotalAccesses());
//            labels.add("Total");
//            values.add(entranceData.getGrantedAccesses());
//            labels.add("Granted");
//            values.add(entranceData.getDeniedAccesses());
//            labels.add("Denied");
//
//            dataSet.setData(values);
//            data.addLabels(labels.toArray(new String[0]));
//        }
//
//        data.addChartDataSet(dataSet);
//        chartModel.setData(data);
//
//        // Chart options
//        LineChartOptions options = new LineChartOptions();
//        chartModel.setOptions(options);
//    }
//    
//    private void createAccessChart(String entranceId) {
//    chartModel = new LineChartModel();
//
//    // Find the entrance data
//    EntranceReportDTO entranceData = reportData.stream()
//            .filter(report -> report.getEntrances().getEntranceDeviceId().equals(entranceId))
//            .findFirst()
//            .orElse(null);
//
//    if (entranceData != null) {
//        // Create a series for access trends
//        LineChartSeries series = new LineChartSeries();
//        series.setLabel("Access Attempts");
//
//        // Add data points (x-axis is the label, y-axis is the value)
//        series.set("Total", entranceData.getTotalAccesses());
//        series.set("Granted", entranceData.getGrantedAccesses());
//        series.set("Denied", entranceData.getDeniedAccesses());
//
//        // Add the series to the chart model
//        chartModel.addSeries(series);
//    }
//
//    // Chart options
//    LineChartOptions options = new LineChartOptions();
//    
//    // Configure x-axis labels
//    CartesianChartOptions cartesianOptions = new CartesianChartOptions();
//    Axis xAxis = new Axis();
//    xAxis.setType("category"); // X-axis is categorical (e.g., "Total", "Granted", "Denied")
//    cartesianOptions.setXAxis(xAxis);
//    
//    // Optionally configure y-axis (e.g., for better scaling)
//    Axis yAxis = new Axis();
//    yAxis.setType("numeric");
//    yAxis.setMin(0); // Ensure y-axis starts at 0
//    cartesianOptions.setYAxis(yAxis);
//
//    // Apply options to the chart
//    chartModel.setOptions(cartesianOptions);
//}
    
    // Summary calculations
    public int getTotalEntrances() {
        return availableEntrances != null ? availableEntrances.size() : 0;    }
    
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
                .mapToLong(dto ->  dto.getGrantedAccesses())
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


    // Utility methods
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", message));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }
}
