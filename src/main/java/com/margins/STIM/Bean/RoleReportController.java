/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.DTO.RoleCountDTO;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.AccessibleEntrancePerRole;
import com.margins.STIM.report.model.EmployeeRoleReport;
import com.margins.STIM.report.model.EmployeesInRole;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.util.Util;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Cacheable;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearAxes;
import org.primefaces.model.charts.axes.cartesian.linear.CartesianLinearTicks;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartOptions;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.pie.PieChartDataSet;
import org.primefaces.model.charts.pie.PieChartModel;
import org.primefaces.model.charts.pie.PieChartOptions;

/**
 *
 * @author PhilipManteAsare
 */
@Named("roleReportController")
@ViewScoped
@Getter
@Setter
@Cacheable(false)
public class RoleReportController implements Serializable {

    @Inject
    private EmployeeRole_Service roleService;

    @Inject
    private Employee employees;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    @Inject
    private UserSession userSession;

    @Inject
    private ReportManager rm;

    private List<RoleCountDTO> allRoles = new ArrayList<>();
    private List<Employee> selectedRoleEmployee = new ArrayList<>();
    private Set<Entrances> selectedEntranceEmployee = new HashSet<>();

    private EmployeeRole selectedRole;
    private BarChartModel employeeCountBarModel;
    private PieChartModel top5roles;
    private PieChartModel top5entranceAccessToRoles;
    private BarChartModel entranceCountBarModel;
    private String selectedRoleName;
    private List<RoleCountDTO> filteredRoles;
    private List<RoleCountDTO> chartRoles;
    private int selectedRowIndex;
    private String employeeRoles;
    private String entrancePerRole;

    @PostConstruct
    public void init() {
        allRoles = roleService.getAllRolesWithCounts();
        createEntranceCountPieChart();
        createTop5RolesPieChart();
        createChart();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setRolesReportBreadcrumb();
    }

    public void viewEmployee(int roleId) {
        selectedRole = roleService.findRoleWithEmployeesById(roleId);
        loadEmployeesForRole();
    }

    public void loadSummary(int roleId) {
        selectedRole = roleService.findRoleWithEmployeesById(roleId);
        loadEmployeesForRole();
        loadEntrancesForRole();
    }

    public void viewEntrances(int roleId) {
        selectedRole = roleService.findRoleWithEmployeesById(roleId);
        loadEntrancesForRole();
    }

    public void loadEmployeesForRole() {
        selectedRoleEmployee.clear();
        if (selectedRole != null && selectedRole.getEmployees() != null) {
            List<Employee> employees = selectedRole.getEmployees();

            if (employees != null) {
                selectedRoleEmployee.addAll(employees);
            }
        }
    }

    public void loadEntrancesForRole() {
        selectedEntranceEmployee.clear();
        if (selectedRole != null && selectedRole.getAccessibleEntrances() != null) {
            Set<Entrances> entrances = selectedRole.getAccessibleEntrances();

            if (entrances != null) {
                selectedEntranceEmployee.addAll(entrances);
            }
        }
    }

    public String formatEmpDate(EmployeeRole role) {
        return DateFormatter.formatDate(role.getCreatedAt());
    }

    public void onChartItemSelect(ItemSelectEvent event) {
        int index = event.getItemIndex();

        // Use chartRoles instead of allRoles
        RoleCountDTO selected = chartRoles.get(index);
        selectedRoleName = selected.getRole().getRoleName();

        // Find the same role in allRoles to get its full index if needed
        for (int i = 0; i < allRoles.size(); i++) {
            if (allRoles.get(i).getRole().getRoleName().equalsIgnoreCase(selectedRoleName)) {
                selectedRowIndex = i;
                break;
            }
        }

        selectedRole = roleService.findRoleWithEmployeesById(selected.getRole().getId());
        loadEmployeesForRole();
        loadEntrancesForRole();
    }

    public void createEmployeeCountBarChart() {
        employeeCountBarModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Employees per Role");

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (RoleCountDTO rc : allRoles) {
            labels.add(rc.getRole().getRoleName());
            values.add(rc.getCount());
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(generateColors(values.size()));
        data.addChartDataSet(dataSet);
        data.setLabels(labels);
        employeeCountBarModel.setData(data);

        // Chart options setup
        BarChartOptions options = new BarChartOptions();
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);

        //  Configure Y-axis scale
        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setOffset(false);
        yAxis.setBeginAtZero(true);

        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setStepSize(1);   // whole number steps
        ticks.setPrecision(0);  //  no decimal places
        yAxis.setTicks(ticks);
        cScales.addYAxesData(yAxis);

        options.setScales(cScales);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        options.setLegend(legend);

        employeeCountBarModel.setOptions(options);
    }

    public void createTop5RolesPieChart() {
        PieChartModel pieModel = new PieChartModel();
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Sort roles by count descending and limit to top 5
        chartRoles = allRoles.stream()
                .sorted((r1, r2) -> Integer.compare(r2.getCount(), r1.getCount()))
                .limit(5)
                .toList();

        for (RoleCountDTO rc : chartRoles) {
            labels.add(rc.getRole().getRoleName());
            values.add(rc.getCount());
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(generateColors(values.size())); // Reuse your color method

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        pieModel.setData(data);

        // Options
        PieChartOptions options = new PieChartOptions();
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        options.setLegend(legend);

        pieModel.setExtender("chartExtender");

        pieModel.setOptions(options);

        // Save it to a field so JSF can render it
        this.top5roles = pieModel;
    }

    public void createEntranceCountBarChart() {
        entranceCountBarModel = new BarChartModel();
        ChartData data = new ChartData();

        BarChartDataSet dataSet = new BarChartDataSet();
        dataSet.setLabel("Accessible Entrances per Role");

        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (RoleCountDTO rc : allRoles) {
            labels.add(rc.getRole().getRoleName());
            values.add(rc.getEntCount());
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(generateColors(values.size()));
        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        entranceCountBarModel.setData(data);

        BarChartOptions options = new BarChartOptions();
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);

        CartesianScales cScales = new CartesianScales();
        CartesianLinearAxes yAxis = new CartesianLinearAxes();
        yAxis.setOffset(false);
        yAxis.setBeginAtZero(true);

        CartesianLinearTicks ticks = new CartesianLinearTicks();
        ticks.setStepSize(1);   // whole number steps
        ticks.setPrecision(0);  //  no decimal places
        yAxis.setTicks(ticks);
        cScales.addYAxesData(yAxis);

        options.setScales(cScales);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        options.setLegend(legend);

        entranceCountBarModel.setOptions(options);
    }

    public void createEntranceCountPieChart() {
        top5entranceAccessToRoles = new PieChartModel(); // Changed to PieChartModel
        ChartData data = new ChartData();

        PieChartDataSet dataSet = new PieChartDataSet();
        List<Number> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Sort by entCount descending and take top 5
        chartRoles = allRoles.stream()
                .sorted((r1, r2) -> Integer.compare(r2.getEntCount(), r1.getEntCount()))
                .limit(5)
                .toList();

        for (RoleCountDTO rc : chartRoles) {
            labels.add(rc.getRole().getRoleName());
            values.add(rc.getEntCount());
        }

        dataSet.setData(values);
        dataSet.setBackgroundColor(generateColors(values.size())); // Reuse your color method

        data.addChartDataSet(dataSet);
        data.setLabels(labels);

        top5entranceAccessToRoles.setData(data);

        // Pie chart options
        PieChartOptions options = new PieChartOptions();
        options.setResponsive(true);
        options.setMaintainAspectRatio(false);

        Legend legend = new Legend();
        legend.setDisplay(true);
        legend.setPosition("top");
        options.setLegend(legend);

        top5entranceAccessToRoles.setExtender("chartExtender");

        top5entranceAccessToRoles.setOptions(options);
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

    public String navigateToHistory(EmployeeRole role) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("role", role);

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying All Access for role " + role.getRoleName()));
        PrimeFaces.current().ajax().update("roleForm");
        //Its best to use full context 
        return "/app/Access/historyMonitoring?faces-redirect=true";

    }

    public void createChart() {
        PrimeFaces.current().executeScript("setReportValues('top5Roles')");
        PrimeFaces.current().executeScript("setReportValues('top5entranceAccessToRoles')");
    }

    public void export() {
        InputStream accessibleEntrancePerRole = Util.toInputStream(entrancePerRole);
        InputStream employeeInRoleCount = Util.toInputStream(employeeRoles);

        List<EmployeeRoleReport> err = new ArrayList<>();

        for (RoleCountDTO rc : allRoles) {
            EmployeeRoleReport roleReport = new EmployeeRoleReport();
            roleReport.setRoleName(rc.getRole().getRoleName());
            String dateCreated = DateFormatter.formatDate(rc.getRole().getCreatedAt());
            roleReport.setDateCreated(dateCreated);
            roleReport.setEmpInRoleCount(rc.getCount());
            roleReport.setRoleAccessibleCount(rc.getEntCount());
            err.add(roleReport);
        }

        rm.setReportFile(ReportGenerator.EMPLOYEES_ROLES_REPORT);
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.addParam("empRoleImage", employeeInRoleCount);
        rm.addParam("empEntranceImage", accessibleEntrancePerRole);
        rm.addParam("roleReport", new JRBeanCollectionDataSource(err));
        rm.setReportDataList(Arrays.asList(new Object()));
        rm.generateReport(ReportOutputFileType.PDF);
    }

    public void exportEmployeesInRole() {
        if (selectedRole == null || selectedRoleEmployee == null || selectedRoleEmployee.isEmpty()) {
            // Show error message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No data to export"));
            return;
        }

        List<EmployeesInRole> empRole = new ArrayList<>();

        for (Employee e : selectedRoleEmployee) {
            EmployeesInRole er = new EmployeesInRole();
            er.setFullName(e.getFullName());
            er.setPhoneNo(e.getPrimaryPhone());
            er.setGhanaCard(e.getGhanaCardNumber());
            er.setEmail(e.getEmail());

            empRole.add(er);
        }

        rm.setReportFile(ReportGenerator.EMPLOYEES_IN_ROLE);
        rm.addParam("roleName", selectedRole.getRoleName());
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.addParam("totalCount", selectedRoleEmployee.size());
        rm.addParam("employeesData", new JRBeanCollectionDataSource(empRole));
        rm.setReportDataList(empRole);
        rm.generateReport(ReportOutputFileType.PDF);
    }
    
    public void exportAccessibleEntrancePerRole(){
        if (selectedRole == null || selectedEntranceEmployee == null) {
            // Show error message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "No data to export"));
            return;
        }
        
        
        List<AccessibleEntrancePerRole> entPerRole = new ArrayList<>();
        
        for(Entrances e: selectedEntranceEmployee){
         AccessibleEntrancePerRole aep = new AccessibleEntrancePerRole();
         aep.setEntranceName(e.getEntranceName());
         aep.setEntranceLocation(e.getEntranceLocation());
         aep.setEntranceMode(e.getEntranceMode().toString());
           
         entPerRole.add(aep);
        }
        
        rm.setReportFile(ReportGenerator.ACCESSIBLE_ENTRANCES_PER_ROLE);
        rm.addParam("roleName", selectedRole.getRoleName());
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.addParam("totalCount", selectedEntranceEmployee.size());
        rm.addParam("entrancesPerRole", new JRBeanCollectionDataSource(entPerRole));
        rm.setReportDataList(entPerRole);
        rm.generateReport(ReportOutputFileType.PDF);
    }
    
    
}
