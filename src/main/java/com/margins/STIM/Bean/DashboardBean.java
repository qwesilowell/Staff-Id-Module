/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.Charts.BaseLineChart;
import com.margins.STIM.Charts.EntranceAccessBarChart;
import com.margins.STIM.Charts.RolePieChart;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.ActivityLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.ActivityLogService;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.RoleCount;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

@Named("dashboard2")
@ViewScoped
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
    private List<RoleCount> rolesWithMostEmployees; 
    private Map<String, String> employeeNameMap;

    // User metrics
    private int userSuccessfulLoginsToday;
    private String userRole;
    private List<Entrances> userAssignedEntrances;
    private List<Employee> userRecentEmployees;
    private List<AccessLog> userRecentAccessAttempts;
    
    @Getter
    private int employeesUserOnboardedToday;

    // Filters
    private String selectedEntranceId;
    private List<Entrances> allEntrances;
    private String dateRange = "today";

    // Charts
    private PieChartModel rolePieChartModel;
    

    private BarChartModel barChartModelEntrance;
    private LineChartModel topEntrancesChartModel;
    
    
    private String verificationChartData;

    @PostConstruct
    public void init() {
        String userRole = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("userRole");
        String username = (String) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("username");
        LocalDate today = LocalDate.now();
        
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now();
        LocalDateTime endofMonth = endOfDay.minusDays(30);
        

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
                    List.of("login"), "Success", startOfDay, endOfDay);
            verificationSuccessRate = calculateVerificationSuccessRate(startOfDay, endOfDay);
            accessSuccessRate = calculateAccessSuccessRate(endofMonth, endOfDay);
            recentLogins = activityLogService.getRecentActivities("login", 5);
            recentEmployees = employeeService.getRecentEmployees(5);
            recentAccessAttempts = accessLogService.getRecentAccessAttempts(5);
            rolesWithMostEmployees = roleService.getTopRolesByEmployeeCount(5);
            allEntrances = entrancesService.findAllEntrances();
        }

        // User metrics
        userSuccessfulLoginsToday = activityLogService.countByActionAndResultAndUser(
                List.of("login"), "Success", username, startOfDay, endOfDay);
        userRole = userRole;
        userAssignedEntrances = entrancesService.getEntrancesForUser(username);
        userRecentEmployees = employeeService.getRecentEmployeesByUser(username, 5);
        userRecentAccessAttempts = accessLogService.getRecentAccessAttemptsByUser(username, 5);
        employeesUserOnboardedToday= activityLogService.countEmployeesOnboardedByLoggedInUserInDay(today);
        // Initialize charts
        initVerificationPieChart();
//        initTop5EntrancesLineChart();

        rolePieChartModel = RolePieChart.generatechart(rolesWithMostEmployees);
        LocalDateTime end = LocalDateTime.now();
//       LocalDateTime start = end.minusDays(30);
        topEntrancesChartModel = BaseLineChart.generateChart(accessLogService.getTop5RecentEntrancesByGrantedAccess(endofMonth, end));
        
//       System.out.println("Map>>>>" + accessLogService.getTop5RecentEntrancesByGrantedAccess(start, end));
        
        barChartModelEntrance = EntranceAccessBarChart.generateChart(accessLogService.countAccessResultsForAllEntrances(startOfDay, endOfDay));


//        initLoginTrendChart();
    }

    private void initVerificationPieChart() {
        // Define the time range (last 30 days)
        LocalDateTime start = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        // Get counts for both Success and Failed in one call
        Map<String, Integer> resultCounts = activityLogService.countBiometricLoginsByResultsInPeriod(start, end);

        // Extract counts (default to 0 if key is missing)
        int successCount = resultCounts.getOrDefault("Success", 0);
        int failCount = resultCounts.getOrDefault("Failed", 0);

        // Format as JSON string for Chart.js
        verificationChartData = String.format("""
        {
            "labels": ["Successful", "Failed"],
            "datasets": [{
                "label": "Biometric Login Outcomes",
                "data": [%d, %d],
                "backgroundColor": ["rgba(54, 162, 235, 0.8)", "rgba(255, 99, 132, 0.8)"],
                "borderColor": ["rgb(54, 162, 235)", "rgb(255, 99, 132)"],
                "borderWidth": 1
            }]
        }
        """, successCount, failCount);

        System.out.println("Chart data: " + verificationChartData);
        System.out.println("Success count: " + successCount + ", Fail count: " + failCount);
    }

    public String getVerificationChartData() {
        return verificationChartData;
    }
    
//    private void initTop5EntrancesLineChart() {
//        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
//        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
//
//        List<Object[]> results = accessLogService.getTop5RecentEntrancesByGrantedAccess(startOfDay, endOfDay);
//
//        Map<String, Integer> chartData = new LinkedHashMap<>();
//        for (Object[] row : results) {
//            String entranceId = (String) row[0];
//            Long count = (Long) row[1];
//
//            // Get entrance name (optional)
//            String entranceName = entrancesService.findEntranceById(entranceId).getEntrance_Name();
//
//            chartData.put(entranceName, count.intValue());
//        }
//
//        topEntrancesChartModel = BaseLineChart.generateChart(chartData);
//    }
    
    
    

// Format AccessLog timestamp
    public String getFormattedTimestamp(AccessLog log) {
        return DateFormatter.formatDateTime(log.getTimestamp());
    }

    // Format ActivityLog timestamp (for Recent Successful Logins)
    public String getFormattedActivityLogTimestamp(ActivityLog log) {
        return DateFormatter.formatDateTime(log.getTimestamp());
    }


    private double calculateVerificationSuccessRate(LocalDateTime start, LocalDateTime end) {
        int success = activityLogService.countByActionAndResultInPeriod(
                List.of("verify_single_finger", "verify_multi_finger", "verify_face"), "success", start, end);
        int total = success + activityLogService.countByActionAndResultInPeriod(
                List.of("verify_single_finger", "verify_multi_finger", "verify_face"), "fail", start, end);
        return total == 0 ? 0 : (success * 100.0) / total;
    }

    private double calculateAccessSuccessRate(LocalDateTime endofMonth, LocalDateTime end) {
        int success = accessLogService.countByResultInPeriod("granted", endofMonth, end);
        int total = success + accessLogService.countByResultInPeriod("denied", endofMonth, end); 
        
        double rtotal = total == 0 ? 0 : (success * 100.0) / total;
        return Math.round(rtotal);      
    }
    
    

//    private void initVerificationPieChart() {
//
//        verificationPieModel = new PieChart()
//                .setData(new PieData()
//                        .addDataset(new PieDataset()
//                                .setData(
//                                        BigDecimal.valueOf(activityLogService.countByActionAndResultInPeriod(
//                                                List.of("verify_single_finger", "verify_multi_finger", "verify_face"),
//                                                "success",
//                                                LocalDate.now().atStartOfDay(),
//                                                LocalDate.now().atTime(23, 59, 59))),
//                                        BigDecimal.valueOf(activityLogService.countByActionAndResultInPeriod(
//                                                List.of("verify_single_finger", "verify_multi_finger", "verify_face"),
//                                                "fail",
//                                                LocalDate.now().atStartOfDay(),
//                                                LocalDate.now().atTime(23, 59, 59)))
//                                )
//                                .setLabel("Verification Outcomes")
//                                .addBackgroundColors(
//                                        new Color(54, 162, 235, 0.8),
//                                        new Color(255, 99, 132, 0.8)
//                                ))
//                        .setLabels("Successful", "Failed"))
//                .setOptions(new PieOptions()
//                        .setPlugins(new Plugins()
//                                .setTitle(new Title()
//                                        .setDisplay(true)
//                                        .setText("Verification Summary"))
//                                .setLegend(new Legend()
//                                        .setPosition(Position.LEFT))
//                        )
//                );
//    }
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
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        if (selectedEntranceId != null) {
//            accessSuccessRate = calculateAccessSuccessRateForEntrance(startOfDay, endOfDay, selectedEntranceId);
            recentAccessAttempts = accessLogService.getRecentAccessAttemptsByEntrance(selectedEntranceId, 5);
        } else {
//            accessSuccessRate = calculateAccessSuccessRate(startOfDay, endOfDay);
            recentAccessAttempts = accessLogService.getRecentAccessAttempts(5);
        }
    }
    
    
    public void entranceaccessupdate(){
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        if (selectedEntranceId != null) {
            barChartModelEntrance = EntranceAccessBarChart.generateChart(accessLogService.countAccessResultsByEntrance(startOfDay, endOfDay, selectedEntranceId));
        }
    }

    private double calculateAccessSuccessRateForEntrance(LocalDateTime start, LocalDateTime end, String entranceId) {
        int success = accessLogService.countByResultAndEntrance("granted", start, end, entranceId);
        int total = success + accessLogService.countByResultAndEntrance("denied", start, end, entranceId);
        return total == 0 ? 0 : (success * 100.0) / total;
    }

    public String getEntranceName(String entranceId) {
        Entrances entrance = entrancesService.findEntranceById(entranceId);
        return entrance != null ? entrance.getEntrance_Name() : entranceId;
    }

    public String getEmployeeNameFromMap(Employee employee) {
        if (employee == null) {
            return "Unknown Employee";
        }
        return employee.getFullName();
    }


    public void logout() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession(); // Ends user session
        ec.redirect(ec.getRequestContextPath() + "/login.xhtml"); // Redirects to login page
    }

}
