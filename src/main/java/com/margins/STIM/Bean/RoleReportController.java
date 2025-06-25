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
import com.margins.STIM.util.RoleCountDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.Cacheable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
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

    private List<RoleCountDTO> allRoles = new ArrayList<>();
    private List<Employee> selectedRoleEmployee = new ArrayList<>();
    private Set<Entrances> selectedEntranceEmployee = new HashSet<>();

    private EmployeeRole selectedRole;
    private BarChartModel employeeCountBarModel;
    private BarChartModel entranceCountBarModel;
    private String selectedRoleName;
    private List<RoleCountDTO> filteredRoles;

    @PostConstruct
    public void init() {
        allRoles = roleService.getAllRolesWithCounts();
        createEmployeeCountBarChart();
        createEntranceCountBarChart();
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
        RoleCountDTO selected = allRoles.get(index);
        selectedRoleName = selected.getRole().getRoleName(); // exact match to compare in the table
        
        selectedRole = roleService.findRoleWithEmployeesById(selected.getRole().getId());
        loadEmployeesForRole();
        loadEntrancesForRole();
        
        //This filters the role after search
//        filteredRoles = allRoles.stream()
//                .filter(r -> r.getRole().getRoleName().equalsIgnoreCase(selectedRoleName))
//                .collect(Collectors.toList());
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
    private List<String> generateColors(int count) {
        List<String> colors = new ArrayList<>();
        String[] baseColors = {
            "#42A5F5", "#66BB6A", "#FFA726", "#AB47BC", "#26C6DA",
            "#EC407A", "#8BC34A", "#FF7043", "#9C27B0", "#00BCD4"
        };
        for (int i = 0; i < count; i++) {
            colors.add(baseColors[i % baseColors.length]);
        }
        return colors;
    }
    
    public String navigateToHistory(EmployeeRole role){
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("role", role);

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying All Access for role " + role.getRoleName()));
        PrimeFaces.current().ajax().update("roleForm");
        //Its best to use full context 
        return "/app/Access/historyMonitoring?faces-redirect=true";
    
    }
}

