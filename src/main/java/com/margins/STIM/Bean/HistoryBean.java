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
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.LazyDataModel;

@Named("historyBean")
@ViewScoped
@Getter
@Setter
public class HistoryBean implements Serializable {

    @EJB
    private AccessLogService accessLogService;
    @EJB
    private EntrancesService entrancesService;
    @EJB
    private Employee_Service employeeService;
    @EJB
    private EmployeeRole_Service roleService;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    private List<AccessLog> recentAccessAttempts;
    private LazyDataModel<AccessLog> lazyAccessLogs;
    private List<AccessLog> filteredAccessAttempts;
    private List<String> allResults;
    private LocalDate selectedDate;
    private EmployeeRole selectedRole;

    //Filters
    private String employeeName;
    private String ghanaCardNumber;
    private String selectedEntranceId;
    private List<Entrances> allEntrances;
    private String employeeId;
    private String entranceId;
    private String result;
    private List<EmployeeRole> allRoles = new ArrayList<>();
    private Integer selectedRoleId;

    private List<LocalDateTime> timeRange;

    private LocalDateTime endTime;

    @PostConstruct
    public void init() {

        allEntrances = entrancesService.findAllEntrances();
        allRoles = roleService.findAllEmployeeRoles();
        allResults = Arrays.asList("granted", "denied");
//        recentAccessAttempts = accessLogService.getRecentAccessAttempts(30);

        String ghanaCardNumberR = (String) FacesContext.getCurrentInstance()
                .getExternalContext().getFlash().get("ghanaCardNumber");

        String employeeNameR = (String) FacesContext.getCurrentInstance()
                .getExternalContext().getFlash().get("employeeName");
        
       EmployeeRole roleR = (EmployeeRole)FacesContext.getCurrentInstance()
                .getExternalContext().getFlash().get("role");

        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap();

        String entranceIdParam = params.get("entranceId");
        String resultParam = params.get("result");

        if ((entranceIdParam != null && !entranceIdParam.isEmpty())
                || (ghanaCardNumberR != null && !ghanaCardNumberR.isEmpty())) {

            if (entranceIdParam != null && !entranceIdParam.isEmpty()) {
                // Set the selected entrance from URL parameter
                this.selectedEntranceId = entranceIdParam;

                if (resultParam != null && !resultParam.isEmpty()) {
                    this.result = resultParam;
                }
            }

            if (ghanaCardNumberR != null && !ghanaCardNumberR.isEmpty()) {
                this.ghanaCardNumber = ghanaCardNumberR;
            }

            // Automatically fetch filtered data if any condition is met
            fetchCriteria();
        } else if (employeeNameR != null && !employeeNameR.isEmpty()) {
            this.employeeName = employeeNameR;
            fetchCriteria();
        }else if (roleR != null) {
            this.selectedRole = roleR;
            this.selectedRoleId = roleR.getId();
            fetchCriteria();
        } 
        else {
            // Load default recent access attempts (only if no filter applied)
            recentAccessAttempts = accessLogService.getRecentAccessAttempts(30);
        }
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setHistoryMonitoringBreadcrumb();
    }

    public List<EmployeeRole> searchRoles(String query) {
        return allRoles.stream()
                .filter(r -> r.getRoleName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void fetchCriteria() {
        if (selectedRole != null) {
            selectedRoleId = selectedRole.getId();
        }
        recentAccessAttempts = accessLogService.filterAccessLog(timeRange, selectedEntranceId, ghanaCardNumber, employeeName, selectedRoleId, result);
    }

    public void reset() {
        timeRange = null;
        result = null;
        selectedEntranceId = null;
        ghanaCardNumber = "";
        employeeName = "";
        fetchCriteria();
    }

    public String getEmployeeName(Employee employee) {
        if (employee == null) {
            return "Unknown Employee";
        }
        return employee.getFullName();
    }

    public String getEntranceName(String entranceId) {
        Entrances entrance = entrancesService.findEntranceById(entranceId);
        return entrance != null ? entrance.getEntrance_Name() : entranceId;
    }

    public String getFormattedTimestamp(AccessLog log) {
        return DateFormatter.formatDateTime(log.getTimestamp());
    }
}
