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
import com.margins.STIM.Charts.LineCharts;
import com.margins.STIM.Charts.RolePieChart;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.ActivityLogService;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.DTO.RoleCount;
import com.margins.STIM.entity.AuditLog;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AnomalyStatus;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.enums.UserType;
import com.margins.STIM.service.AccessAnomalyService;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.EmployeeEntranceStateService;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;
//import software.xdev.chartjs.model.options.Legend;

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
    @Inject
    private UserSession userSession;
    @Inject
    private AuditLogService auditLogService;
    @Inject
    private EmployeeEntranceStateService empEntStateService;
    @Inject
    private LineCharts lineCharts;
    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private AccessAnomalyService anomalyService;

    // Admin metrics
    private int totalEmployees;
    private int totalRoles;
    private int totalEntrances;
    private int employeesOnboardedToday;
    private int employeesOnboardedWeek;
    private int employeesOnboardedMonth;
    private int employeesOnboardedYear;
    private int successfulLoginsToday;
    private Integer entranceId;
    private double verificationSuccessRate;
    private double accessSuccessRate;
    private List<AuditLog> recentLogins;
    private List<AuditLog> recentActivities;
    private List<Employee> recentEmployees;
    private List<AccessLog> recentAccessAttempts;
    private List<RoleCount> rolesWithMostEmployeesLimit = new ArrayList<>();
    private List<RoleCount> rolesWithMostEmployees = new ArrayList<>();
    private LocalDate[] dateRange;
    private LocalDate startFrom;
    private LocalDate endAt;

    private Map<String, String> employeeNameMap;

    // Other metrics
    private int userSuccessfulLoginsToday;
    private String userRole;
    private List<Entrances> userAssignedEntrances;
    private List<AccessLog> userRecentAccessAttempts;
    private LineChartModel mostAccessedEntrance;
    private List<EmployeeEntranceState> currentStates;
    private Users currentUser;

    private int employeesUserOnboardedToday;
    private long unattendedCount;
    private long pendingCount;
    private long resolvedCount;

    // Filters
    private String selectedEntranceId;
    private List<Entrances> allEntrances;

    // Charts
    private PieChartModel rolePieChartModel;
    private PieChartModel rolePieChartModelS;
    private BarChartModel barChartModelEntrance;
    private LineChartModel topEntrancesChartModel;
    private LineChartModel lineChartModel;

    private String verificationChartData;

    @PostConstruct
    public void init() {
        userRole = userSession.userRoleName();
        String username = userSession.getUsername();
        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = LocalDateTime.now();
        LocalDateTime endofMonth = endOfDay.minusDays(30);

        endAt = LocalDate.now();
        startFrom = endAt.minusDays(30);
        updateMostAccessed();
        currentUser = userSession.getCurrentUser();
       anomalyCount();

        currentStates = empEntStateService.findRecentEmployeeEntranceStates(20);
        // Admin metrics {find a way to make it run if the role is admin or consider using the usertype you get ?}
        if (userSession.checkAdmin()) {
            totalEmployees = employeeService.getTotalEmployees();
            totalRoles = roleService.getTotalRoles();
            totalEntrances = entrancesService.getTotalEntrances();
            employeesOnboardedToday = employeeService.countEmployeesOnboarded(startOfDay, endOfDay);
            employeesOnboardedWeek = employeeService.countEmployeesOnboarded(today.minusDays(6).atStartOfDay(), endOfDay);
            employeesOnboardedMonth = employeeService.countEmployeesOnboarded(today.withDayOfMonth(1).atStartOfDay(), endOfDay);
            employeesOnboardedYear = employeeService.countEmployeesOnboarded(today.withDayOfYear(1).atStartOfDay(), endOfDay);
            successfulLoginsToday = auditLogService.countByActionAndResultInPeriod(
                    AuditActionType.LOGIN, ActionResult.SUCCESS, startOfDay, endOfDay);
            accessSuccessRate = calculateAccessSuccessRate(endofMonth, endOfDay);
            recentLogins = auditLogService.getRecentActivities(5);
            recentEmployees = employeeService.getRecentEmployees(5);
            recentAccessAttempts = accessLogService.getRecentAccessAttempts(5);
            rolesWithMostEmployeesLimit = roleService.getTopRolesByEmployeeCount(5);

            allEntrances = entrancesService.findAllEntrances();

            //G'S IMPLEMENTATIO 
            rolesWithMostEmployees = roleService.getTopRolesByEmployeeCount(5);
            createRolePieChart();
            lineChartModel = buildGrantedDeniedLineChart(endofMonth, endOfDay);
        }

        // User metrics
        userSuccessfulLoginsToday = auditLogService.countByActionAndResultAndUser(
                AuditActionType.LOGIN, ActionResult.SUCCESS, userSession.getCurrentUser(), startOfDay, endOfDay);
        userAssignedEntrances = entrancesService.getEntrancesForUser(userSession.getGhanaCardNumber());
        userRecentAccessAttempts = accessLogService.getRecentAccessAttemptsByUser(username, 5);

        totalEmployees = employeeService.getTotalEmployees();
        totalRoles = roleService.getTotalRoles();
        totalEntrances = entrancesService.getTotalEntrances();
        employeesOnboardedToday = employeeService.countEmployeesOnboarded(startOfDay, endOfDay);
        employeesOnboardedWeek = employeeService.countEmployeesOnboarded(today.minusDays(6).atStartOfDay(), endOfDay);
        employeesOnboardedMonth = employeeService.countEmployeesOnboarded(today.withDayOfMonth(1).atStartOfDay(), endOfDay);
        employeesOnboardedYear = employeeService.countEmployeesOnboarded(today.withDayOfYear(1).atStartOfDay(), endOfDay);
        successfulLoginsToday = auditLogService.countByActionAndResultInPeriod(
                AuditActionType.LOGIN, ActionResult.SUCCESS, startOfDay, endOfDay);
        accessSuccessRate = calculateAccessSuccessRate(endofMonth, endOfDay);
        recentActivities = auditLogService.getRecentActivitiesByUser(userSession.getCurrentUser(), 7);
        recentEmployees = employeeService.getRecentEmployees(5);
        recentAccessAttempts = accessLogService.getRecentAccessAttempts(5);
        rolesWithMostEmployeesLimit = roleService.getTopRolesByEmployeeCount(5);
        allEntrances = entrancesService.findAllEntrances();

        rolePieChartModel = RolePieChart.generatechart(rolesWithMostEmployees);
        LocalDateTime end = LocalDateTime.now();
        topEntrancesChartModel = BaseLineChart.generateChart(accessLogService.getTop5RecentEntrancesByGrantedAccess(endofMonth, end));

        barChartModelEntrance = EntranceAccessBarChart.generateChart(accessLogService.countAccessResultsForAllEntrances(endofMonth, endOfDay));

    }

    public void updateMostAccessed() {
        if (startFrom != null && endAt != null) {

            mostAccessedEntrance = lineCharts.buildEntryExitLineChart(startFrom.atStartOfDay(), endAt.atTime(LocalTime.MAX));
        } else {
            if (endAt == null) {
                endAt = LocalDate.now(); // Default to today
            }
            if (startFrom == null) {
                startFrom = endAt.minusMonths(6); // Default to 30 days ago
            }

            LocalDateTime end = endAt.atTime(LocalTime.MAX);
            LocalDateTime start = startFrom.atTime(LocalTime.MAX);

            mostAccessedEntrance = lineCharts.buildEntryExitLineChart(start, end);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Date range not specified.", " Defaulting to the last 6 months."));
        }
    }

    public void createRolePieChart() {
        rolePieChartModelS = new PieChartModel();
        ChartData data = new ChartData();
        PieChartDataSet dataSet = new PieChartDataSet();

        // Extract data from RoleCount objects
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (RoleCount roleCount : rolesWithMostEmployees) {
            labels.add(roleCount.getRoleName());
            values.add(roleCount.getCount());
        }

        dataSet.setData(values);

        // Generate colors dynamically for potentially 100+ roles
        List<String> colors = generateColors(rolesWithMostEmployees.size());
        dataSet.setBackgroundColor(colors);

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        rolePieChartModelS.setData(data);

        PieChartOptions options = new PieChartOptions();
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("right");
        options.setLegend(legend);

        // Make chart responsive
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);

        rolePieChartModelS.setOptions(options);
    }

    public LineChartModel buildGrantedDeniedLineChart(LocalDateTime start, LocalDateTime end) {
        LineChartModel model = new LineChartModel();
        ChartData data = new ChartData();

        // Fetch structured access log data
        Map<String, Map<String, Integer>> accessData = accessLogService.getTop5RecentEntrancesByAccessResult(start, end);
        if (accessData == null || accessData.isEmpty()) {
            System.out.println("No access data returned.");
            return new LineChartModel(); // still return an empty model

        } else {
            System.out.println("ACCESS DATA FOUND>>>>>>>>>>>>>>>>>> ");
        }

        List<String> labels = new ArrayList<>(accessData.keySet());
        List<Object> grantedCounts = new ArrayList<>();
        List<Object> deniedCounts = new ArrayList<>();

        for (String entrance : labels) {
            Map<String, Integer> resultCounts = accessData.get(entrance);
            grantedCounts.add(resultCounts.getOrDefault("GRANTED", 0));
            deniedCounts.add(resultCounts.getOrDefault("DENIED", 0));
        }

        // Dataset for Granted
        LineChartDataSet grantedSet = new LineChartDataSet();
        grantedSet.setLabel("GRANTED");
        grantedSet.setData(grantedCounts);
        grantedSet.setBorderColor("#4CAF50"); // green
        grantedSet.setFill(false);

        // Dataset for Denied
        LineChartDataSet deniedSet = new LineChartDataSet();
        deniedSet.setLabel("DENIED");
        deniedSet.setData(deniedCounts);
        deniedSet.setBorderColor("#F44336"); // red
        deniedSet.setFill(false);

        data.setLabels(labels);
        data.addChartDataSet(grantedSet);
        data.addChartDataSet(deniedSet);

        model.setData(data);

        // Options
        LineChartOptions options = new LineChartOptions();
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);
        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("right");
        options.setLegend(legend);

        model.setOptions(options);
        return model;
    }

    public void onChartItemSelect(ItemSelectEvent event) {
        try {
            System.out.println("ItemSelected>>>>>>>>>>>>>>>>>> " + event.getDataSetIndex());
            System.out.println("ItemSelected>>>>>>>>>>>>>>>>>> " + event.getItemIndex());
            System.out.println("ItemSelected>>>>>>>>>>>>>>>>>> " + event.getSource());

            org.primefaces.component.linechart.LineChart chart = (org.primefaces.component.linechart.LineChart) event.getSource();

            // > org.primefaces.component.linechart.LineChart@6f99863e|
//            LineChartModel model = (LineChartModel) lineChartModel;
//            ChartData data = model.getData();
//
//            String clickedEntrance = ((List<String>) data.getLabels()).get(event.getItemIndex()); // x-axis label
//            String resultClicked = ((List<ChartDataSet>) data.getDataSet()).get(event.getDataSetIndex()).getLabel().toLowerCase();
//
//            // Store in Flash
//            Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
//            flash.put("selectedEntranceName", clickedEntrance);
//            flash.put("result", resultClicked);
//            flash.put("timeRange", Arrays.asList(endofMonth, endOfDay)); // assuming you used those when building the chart
//
//            flash.setKeepMessages(true); // Optional: if you want to show a FacesMessage
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> generateColors(int count) {
        List<String> colors = new ArrayList<>();

        // Base color palette - you can customize these
        String[] baseColors = {
            "#FF6384", "#FFECA1", "9C27B0", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF",
            "#FF6384", "#FF9F40", "#4BC0C0", "#C9CBCF", "#FF6384",
            "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40",
            "#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#26C6DA",
            "#EC407A", "#8BC34A", "#FF7043", "#DFC57B", "#00BCD4"
        };

        // If we need more colors than our base palette, generate them
        for (int i = 0; i < count; i++) {
            if (i < baseColors.length) {
                colors.add(baseColors[i]);
            } else {
                // Generate additional colors using HSL
                float hue = (i * 137.508f) % 360; // Golden angle approximation
                String color = String.format("hsl(%.1f, 70%%, 50%%)", hue);
                colors.add(color);
            }
        }
        return colors;
    }

//    private void initVerificationPieChart() {
//        // Define the time range (last 30 days)
//        LocalDateTime start = LocalDateTime.now().minusDays(30).withHour(0).withMinute(0).withSecond(0).withNano(0);
//        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999);
//
//        // Get counts for both Success and Failed in one call
//        Map<String, Integer> resultCounts = activityLogService.countBiometricLoginsByResultsInPeriod(start, end);
//
//        // Extract counts (default to 0 if key is missing)
//        int successCount = resultCounts.getOrDefault("Success", 0);
//        int failCount = resultCounts.getOrDefault("Failed", 0);
//
//        // Format as JSON string for Chart.js
//        verificationChartData = String.format("""
//        {
//            "labels": ["Successful", "Failed"],
//            "datasets": [{
//                "label": "Biometric Login Outcomes",
//                "data": [%d, %d],
//                "backgroundColor": ["rgba(54, 162, 235, 0.8)", "rgba(255, 99, 132, 0.8)"],
//                "borderColor": ["rgb(54, 162, 235)", "rgb(255, 99, 132)"],
//                "borderWidth": 1
//            }]
//        }
//        """, successCount, failCount);
//
//        System.out.println("Chart data: " + verificationChartData);
//        System.out.println("Success count: " + successCount + ", Fail count: " + failCount);
//    }
    public String getVerificationChartData() {
        return verificationChartData;
    }

    public String getFormattedTimestamp(AccessLog log) {
        return DateFormatter.forDateTimes(log.getTimestamp());
    }

    public String timeString(LocalDateTime dt) {
        return DateFormatter.forDateTimes(dt);
    }

    public String getFormattedTime(AuditLog log) {
        return DateFormatter.formatDateTime(log.getCreatedOn());
    }

    private double calculateAccessSuccessRate(LocalDateTime endofMonth, LocalDateTime end) {
        int success = accessLogService.countByResultInPeriod("granted", endofMonth, end);
        int total = success + accessLogService.countByResultInPeriod("denied", endofMonth, end);

        double rtotal = total == 0 ? 0 : (success * 100.0) / total;
        return Math.round(rtotal);
    }

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

    public void entranceaccessupdate() {
        LocalDate entStartDate;
        LocalDate entEndDate;
        if (dateRange != null && dateRange.length == 2) {
            entStartDate = dateRange[0];
            entEndDate = dateRange[1];
        } else {
            LocalDate today = LocalDate.now();
            entEndDate = today;
            entStartDate = today.minusDays(7);
        }
        LocalDateTime startOfDay = entStartDate.atStartOfDay();
        LocalDateTime endOfDay = entEndDate.atTime(23, 59, 59);

        if (entranceId != null) {
            System.out.println("Selected Entrance ID: " + entranceId);
            barChartModelEntrance = EntranceAccessBarChart.generateChart(
                    accessLogService.countAccessResultsByEntrance(startOfDay, endOfDay, entranceId));
        } else {
            barChartModelEntrance = EntranceAccessBarChart.generateChart(
                    accessLogService.countAccessResultsForAllEntrances(startOfDay, endOfDay));
        }
    }

    private double calculateAccessSuccessRateForEntrance(LocalDateTime start, LocalDateTime end, String entranceId) {
        int success = accessLogService.countByResultAndEntrance("granted", start, end, entranceId);
        int total = success + accessLogService.countByResultAndEntrance("denied", start, end, entranceId);
        return total == 0 ? 0 : (success * 100.0) / total;
    }

    public String getEntranceName(int entranceId) {
        Entrances entrance = entrancesService.findEntranceById(entranceId);
        return entrance != null ? entrance.getEntranceName() : "Entrance not found";
    }

    public String getEmployeeName(Employee employee) {
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

    public void anomalyCount() {
        if (currentUser.getUserType() == UserType.ADMIN) {
            unattendedCount = anomalyService.countByStatus(AnomalyStatus.UNATTENDED);
            pendingCount = anomalyService.countByStatus(AnomalyStatus.PENDING);
            resolvedCount = anomalyService.countByStatus(AnomalyStatus.RESOLVED);
            
        } else {
            unattendedCount = anomalyService.countByStatus(AnomalyStatus.UNATTENDED, currentUser);
            pendingCount = anomalyService.countByStatus(AnomalyStatus.PENDING, currentUser);
            resolvedCount = anomalyService.countByStatus(AnomalyStatus.RESOLVED, currentUser);        
        }
    }
    ///Trial
    private Map<Integer, Boolean> entranceExpansionMap = new HashMap<>();

    public Map<Integer, Boolean> getEntranceExpansionMap() {
        return entranceExpansionMap;
    }

    public void toggleEntranceExpansion(Integer entranceId) {
        boolean current = entranceExpansionMap.getOrDefault(entranceId, false);
        entranceExpansionMap.put(entranceId, !current);
    }

    public boolean getEntranceExpanded(Integer entranceId) {
        return entranceExpansionMap.getOrDefault(entranceId, false);
    }

    public void setupBreadcrumb() {
        breadcrumbBean.resetToDashboard();
    }
}
