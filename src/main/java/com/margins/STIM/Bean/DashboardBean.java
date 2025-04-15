/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.ActivityLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.ActivityLogService;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.RoleCount;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.line.LineChartModel;
import software.xdev.chartjs.model.charts.PieChart;
import software.xdev.chartjs.model.color.Color;
import software.xdev.chartjs.model.data.PieData;
import software.xdev.chartjs.model.dataset.PieDataset;
import software.xdev.chartjs.model.options.Legend;
import software.xdev.chartjs.model.options.PieOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.Legend.Position;

@Named("dashboard2")
@SessionScoped
@Getter
@Setter
public class DashboardBean implements Serializable {

    @EJB
    private ActivityLogService activityLogService;
    @EJB
    private AccessLogService accessLogService;
    @EJB
    private Employee_Service employeeService;
    @EJB
    private EmployeeRole_Service roleService;
    @EJB
    private EntrancesService entrancesService;

    // Admin metrics
    private int totalEmployees;
    private int totalRoles;
    private int totalEntrances;
    private int employeesOnboardedToday;
    private int employeesOnboardedWeek;
    private int employeesOnboardedMonth;
    private int employeesOnboardedYear;
    private int successfulLoginsToday;
    private double verificationSuccessRate;
    private double accessSuccessRate;
    private List<ActivityLog> recentLogins;
    private List<Employee> recentEmployees;
    private List<AccessLog> recentAccessAttempts;
    private List<RoleCount> rolesWithMostEmployees; // Updated to use standalone RoleCount

    // User metrics
    private int userSuccessfulLoginsToday;
    private String userRole;
    private List<Entrances> userAssignedEntrances;
    private List<Employee> userRecentEmployees;
    private List<AccessLog> userRecentAccessAttempts;

    // Filters
    private Entrances selectedEntrance;
    private List<Entrances> allEntrances;
    private String dateRange = "today";

    // Charts
    private PieChart verificationPieModel;
    
    private BarChartModel onboardedBarChart;
    private LineChartModel loginTrendChart;

    @PostConstruct
    public void init() {
        String userRole = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userRole");
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);

        // Admin metrics
        if ("Admin".equals(userRole)) {
            totalEmployees = employeeService.getTotalEmployees();
            totalRoles = roleService.getTotalRoles();
            totalEntrances = entrancesService.getTotalEntrances();
            employeesOnboardedToday = employeeService.countEmployeesOnboarded(startOfDay, endOfDay);
            employeesOnboardedWeek = employeeService.countEmployeesOnboarded(today.minusDays(6).atStartOfDay(), endOfDay);
            employeesOnboardedMonth = employeeService.countEmployeesOnboarded(today.withDayOfMonth(1).atStartOfDay(), endOfDay);
            employeesOnboardedYear = employeeService.countEmployeesOnboarded(today.withDayOfYear(1).atStartOfDay(), endOfDay);
            successfulLoginsToday = activityLogService.countByActionAndResultInPeriod(
                    List.of("login"), "success", startOfDay, endOfDay);
            verificationSuccessRate = calculateVerificationSuccessRate(startOfDay, endOfDay);
            accessSuccessRate = calculateAccessSuccessRate(startOfDay, endOfDay);
            recentLogins = activityLogService.getRecentActivities("login", 5);
            recentEmployees = employeeService.getRecentEmployees(5);
            recentAccessAttempts = accessLogService.getRecentAccessAttempts(5);
            rolesWithMostEmployees = roleService.getTopRolesByEmployeeCount(5);
            allEntrances = entrancesService.findAllEntrances();
        }

        // User metrics
        userSuccessfulLoginsToday = activityLogService.countByActionAndResultAndUser(
                List.of("login"), "success", username, startOfDay, endOfDay);
        userRole = userRole;
        userAssignedEntrances = entrancesService.getEntrancesForUser(username);
        userRecentEmployees = employeeService.getRecentEmployeesByUser(username, 5);
        userRecentAccessAttempts = accessLogService.getRecentAccessAttemptsByUser(username, 5);

        // Initialize charts
        initVerificationPieChart();
//        initOnboardedBarChart();
//        initLoginTrendChart();
    }

    // ... rest of the methods (unchanged) ...
    private double calculateVerificationSuccessRate(LocalDateTime start, LocalDateTime end) {
        int success = activityLogService.countByActionAndResultInPeriod(
                List.of("verify_single_finger", "verify_multi_finger", "verify_face"), "success", start, end);
        int total = success + activityLogService.countByActionAndResultInPeriod(
                List.of("verify_single_finger", "verify_multi_finger", "verify_face"), "fail", start, end);
        return total == 0 ? 0 : (success * 100.0) / total;
    }

    private double calculateAccessSuccessRate(LocalDateTime start, LocalDateTime end) {
        int success = accessLogService.countByResultInPeriod("granted", start, end);
        int total = success + accessLogService.countByResultInPeriod("denied", start, end);
        return total == 0 ? 0 : (success * 100.0) / total;
    }

    private void initVerificationPieChart() {
        verificationPieModel = new PieChart()
                .setData(new PieData()
                        .addDataset(new PieDataset()
                                .setData(
                                        BigDecimal.valueOf(activityLogService.countByActionAndResultInPeriod(
                                                List.of("verify_single_finger", "verify_multi_finger", "verify_face"),
                                                "success",
                                                LocalDate.now().atStartOfDay(),
                                                LocalDate.now().atTime(23, 59, 59))),
                                        BigDecimal.valueOf(activityLogService.countByActionAndResultInPeriod(
                                                List.of("verify_single_finger", "verify_multi_finger", "verify_face"),
                                                "fail",
                                                LocalDate.now().atStartOfDay(),
                                                LocalDate.now().atTime(23, 59, 59)))
                                )
                                .setLabel("Verification Outcomes")
                                .addBackgroundColors(
                                        new Color(54, 162, 235, 0.8),
                                        new Color(255, 99, 132, 0.8)
                                ))
                        .setLabels("Successful", "Failed"))
                .setOptions(new PieOptions()
                        .setPlugins(new Plugins()
                                .setTitle(new Title()
                                        .setDisplay(true)
                                        .setText("Verification Summary"))
                                .setLegend(new Legend()
                                        .setPosition(Position.LEFT))
                        )
                );
    }


//    private void initOnboardedBarChart() {
//        onboardedBarChart = new BarChartModel();
//        ChartData data = new ChartData();
//
//        BarChartDataSet dataSet = new BarChartDataSet();
//        dataSet.setLabel("Employees Onboarded");
//
//        LocalDate today = LocalDate.now();
//        List<String> labels = new java.util.ArrayList<>();
//        List<Number> values = new java.util.ArrayList<>();
//        for (int i = 6; i >= 0; i--) {
//            LocalDate date = today.minusDays(i);
//            LocalDateTime start = date.atStartOfDay();
//            LocalDateTime end = date.atTime(23, 59, 59);
//            int count = employeeService.countEmployeesOnboarded(start, end);
//            labels.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")));
//            values.add(count);
//        }
//
//        dataSet.setData(values);
//        dataSet.setBackgroundColor("rgb(75, 192, 192)"); // Teal
//        data.setLabels(labels);
//        data.addChartDataSet(dataSet);
//
//        onboardedBarChart.setData(data);
//
//        // Chart options
//        ChartOptions options = new ChartOptions();
//        Title title = new Title();
//        title.setDisplay(true);
//        title.setText("Employees Onboarded This Week");
//        options.setTitle(title);
//
//        org.primefaces.model.chart.Axes axes = new org.primefaces.model.chart.Axes();
//        org.primefaces.model.chart.Axis xAxis = new org.primefaces.model.chart.Axis();
//        xAxis.setLabel("Date");
//        axes.setXAxis(xAxis);
//        org.primefaces.model.chart.Axis yAxis = new org.primefaces.model.chart.Axis();
//        yAxis.setLabel("Count");
//        yAxis.setMin(0);
//        axes.setYAxis(yAxis);
//        options.setAxes(axes);
//
//        onboardedBarChart.setOptions(options);
//    }
//
//    private void initLoginTrendChart() {
//        loginTrendChart = new LineChartModel();
//        ChartData data = new ChartData();
//
//        LineChartDataSet dataSet = new LineChartDataSet();
//        dataSet.setLabel("Successful Logins");
//
//        LocalDate today = LocalDate.now();
//        List<String> labels = new java.util.ArrayList<>();
//        List<Number> values = new java.util.ArrayList<>();
//        for (int i = 6; i >= 0; i--) {
//            LocalDate date = today.minusDays(i);
//            LocalDateTime start = date.atStartOfDay();
//            LocalDateTime end = date.atTime(23, 59, 59);
//            int count = activityLogService.countByActionAndResultInPeriod(
//                    List.of("login"), "success", start, end);
//            labels.add(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")));
//            values.add(count);
//        }
//
//        dataSet.setData(values);
//        dataSet.setFill(false);
//        dataSet.setBorderColor("rgb(54, 162, 235)"); // Blue
//        data.setLabels(labels);
//        data.addChartDataSet(dataSet);
//
//        loginTrendChart.setData(data);
//
//        // Chart options
//        ChartOptions options = new ChartOptions();
//        Title title = new Title();
//        title.setDisplay(true);
//        title.setText("Successful Logins (Last 7 Days)");
//        options.setTitle(title);
//
//        org.primefaces.model.chart.Axes axes = new org.primefaces.model.chart.Axes();
//        org.primefaces.model.chart.Axis xAxis = new org.primefaces.model.chart.Axis();
//        xAxis.setLabel("Date");
//        axes.setXAxis(xAxis);
//        org.primefaces.model.chart.Axis yAxis = new org.primefaces.model.chart.Axis();
//        yAxis.setLabel("Count");
//        yAxis.setMin(0);
//        axes.setYAxis(yAxis);
//        options.setAxes(axes);
//
//        loginTrendChart.setOptions(options);
//    }

    public void updateStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        if (selectedEntrance != null) {
            accessSuccessRate = calculateAccessSuccessRateForEntrance(startOfDay, endOfDay, selectedEntrance);
            recentAccessAttempts = accessLogService.getRecentAccessAttemptsByEntrance(selectedEntrance.getEntrance_Device_ID(), 5);
        } else {
            accessSuccessRate = calculateAccessSuccessRate(startOfDay, endOfDay);
            recentAccessAttempts = accessLogService.getRecentAccessAttempts(5);
        }
    }

    private double calculateAccessSuccessRateForEntrance(LocalDateTime start, LocalDateTime end, Entrances entrance) {
        int success = accessLogService.countByResultAndEntrance("granted", start, end, entrance.getEntrance_Device_ID());
        int total = success + accessLogService.countByResultAndEntrance("denied", start, end, entrance.getEntrance_Device_ID());
        return total == 0 ? 0 : (success * 100.0) / total;
    }
    public void logout() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession(); // Ends user session
        ec.redirect(ec.getRequestContextPath() + "/login.xhtml"); // Redirects to login page
    }

}
