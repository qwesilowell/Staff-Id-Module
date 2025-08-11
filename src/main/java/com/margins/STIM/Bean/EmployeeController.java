/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.EmployeeReport;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.ReportsService;
import com.margins.STIM.util.DateFormatter;
import com.margins.STIM.util.JSF;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.primefaces.PrimeFaces;

/**
 *
 * @author PhilipManteAsare
 */
@Named("empReportController")
@ViewScoped
@Getter
@Setter
public class EmployeeController implements Serializable {

    @EJB
    private Employee_Service employeeService;

    @EJB
    private EmployeeRole_Service roleService;

    @EJB
    private ReportsService reportService;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    @Inject
    private ReportManager rm;

    @Inject
    private UserSession userSession;

    private List<Employee> employees;
    private List<EmployeeRole> allRoles;
    private Set<Entrances> selectedEmployeeEntrances = new HashSet<>();
    private Set<Entrances> selectedEmployeeCustomEntrances = new HashSet<>();

    private String employeeName;
    private String ghanaCardNumber;
    private EmployeeRole selectedRole;
    private Employee selectedEmployee;
    private List<LocalDateTime> timeRange;
    String BASE_URL = JSF.getContextURL() + "/";

    @PostConstruct
    public void init() {
        employees = employeeService.findAllEmployees();
        allRoles = roleService.findAllEmployeeRoles();
    }

    public void export() {

        List<EmployeeReport> empReport = new ArrayList<>();

        for (Employee e : employees) {
            EmployeeReport employeeReport = new EmployeeReport();

            employeeReport.setEmpName(e.getFullName());
            employeeReport.setGhanaCardNumber(e.getGhanaCardNumber());
            employeeReport.setEmail(e.getEmail());
            employeeReport.setGender(e.getGender());
            employeeReport.setPhoneNumber(e.getPrimaryPhone());
            if (e.getEmploymentStatus() == null) {
                String TemporaryStatus = "Temp";
                employeeReport.setEmploymentStatus(TemporaryStatus);
            } else {
                employeeReport.setEmploymentStatus(e.getEmploymentStatus().getEmpstatus());
            }
            employeeReport.setRole(e.getRole().getRoleName());
            String dateOnboarded = DateFormatter.formatDate(e.getCreatedAt());
            employeeReport.setDateOnboarded(dateOnboarded);

            empReport.add(employeeReport);

        }
        rm.addParam("reportTitle", "EMPLOYEE REPORT");
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        rm.setReportFile(ReportGenerator.EMPLOYEE_REPORT);
        rm.setReportDataList(empReport);
        rm.generateReport(ReportOutputFileType.PDF);
    }

    public void download() {
        List<Object> dummyData = Collections.singletonList(new Object());

        rm.addParam("empName", selectedEmployee.getFullName());
        rm.addParam("role", selectedEmployee.getRole().getRoleName());
        rm.addParam("ghanaCardNumber", selectedEmployee.getGhanaCardNumber());
        if (selectedEmployee.getEmploymentStatus() == null) {
            String TemporaryStatus = "Temp";
            rm.addParam("employmentStatus", TemporaryStatus);
        } else {
            rm.addParam("employmentStatus", selectedEmployee.getEmploymentStatus().getEmpstatus());
        }
        rm.addParam("phoneNumber", selectedEmployee.getPrimaryPhone());
        rm.addParam("printedBy", userSession.getCurrentUser().getUsername());
        String dateOnboarded = DateFormatter.formatDate(selectedEmployee.getCreatedAt());
        rm.addParam("dateOnboarded", dateOnboarded);
        rm.addParam("reportTitle", "EMPLOYEE ACCESSIBLE ENTRANCES");
        rm.addParam("roleEntrances", new JRBeanCollectionDataSource(selectedEmployeeEntrances));
        rm.addParam("customEntrances", new JRBeanCollectionDataSource(selectedEmployeeCustomEntrances));
        rm.setReportFile(ReportGenerator.ACCESSIBLE_EMPLOYEE_ENTRANCES);
        rm.setReportDataList(dummyData);
        rm.generateReport(ReportOutputFileType.PDF);
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setEmployeeReportBreadcrumb();
    }

    public void fetchCriteria() {
        employees = reportService.filterEmployees(employeeName, ghanaCardNumber, selectedRole, timeRange);
    }

    public String formatEmpDate(Employee employee) {
        return DateFormatter.formatDate(employee.getCreatedAt());
    }

    public List<EmployeeRole> searchRoles(String query) {
        return allRoles.stream()
                .filter(r -> r.getRoleName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public void viewEntrances(Employee employee) {
        selectedEmployee = employeeService.findEmployeeByGhanaCard(employee.getGhanaCardNumber());
        loadEntrancesForRole();
    }

    public void loadEntrancesForRole() {
        selectedEmployeeEntrances.clear();
        selectedEmployeeCustomEntrances.clear();// clear old data first

        if (selectedEmployee != null && selectedEmployee.getRole() != null) {
            Set<Entrances> entrances = selectedEmployee.getRole().getAccessibleEntrances();
            List<Entrances> customEntrance = selectedEmployee.getCustomEntrances();

            if (entrances != null && !entrances.isEmpty()) {
                selectedEmployeeEntrances.addAll(entrances);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No entrances assigned to this role."));
            }
            if (customEntrance != null && !customEntrance.isEmpty()) {
                selectedEmployeeCustomEntrances.addAll(customEntrance);
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "No customEntrances assigned to " + selectedEmployee.getFirstname()));
            }

        }
    }

    public void reset() {
        ghanaCardNumber = "";
        employeeName = "";
        selectedRole = null;
        timeRange = null;
        fetchCriteria();
    }

    //How to properly navigate 
    public String navigateToHistory(String ghanacardNumber) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("ghanaCardNumber", ghanacardNumber);

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying Recent Access for GHANA CARD NUMBER " + ghanacardNumber));
        PrimeFaces.current().ajax().update("employeeForm");
        //Its best to use full context 
        return "/app/Access/historyMonitoring?faces-redirect=true";
    }

    public String navigateToHistoryName(String employeeName) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("employeeName", employeeName);

        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying Recent Access for employee " + employeeName));
        PrimeFaces.current().ajax().update("employeeForm");
        //Its best to use full context 
        return "/app/Access/historyMonitoring?faces-redirect=true";
    }
}
