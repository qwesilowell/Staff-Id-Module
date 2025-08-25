/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.enums.LocationState;
import com.margins.STIM.report.ReportGenerator;
import com.margins.STIM.report.ReportManager;
import com.margins.STIM.report.model.OccupantState;
import com.margins.STIM.report.util.ReportOutputFileType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.EmployeeEntranceStateService;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
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
@Named("employeeStateBean")
@Getter
@Setter
@ViewScoped
public class EmployeeStateBean implements Serializable {

    @Inject
    private Employee_Service employeeService;

    @Inject
    private EmployeeEntranceStateService stateService;

    @Inject
    private EntrancesService entranceService;

    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;

    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private ReportManager rm;

    private List<EmployeeEntranceState> allStates;
    private List<EmployeeEntranceState> filteredStates;
    private List<Entrances> allEntrances;
    private List<LocationState> locationStates = Arrays.asList(LocationState.values());

    private Employee selectedEmployee;
    private Entrances selectedEntrance;
    private LocationState locationStateFilter;

    private EmployeeEntranceState selectedState;
    private String newState;
    private String resetReason;

    @PostConstruct
    public void init() {
        allStates = stateService.findAllEmployeeEntranceStates();
        filteredStates = new ArrayList<>(allStates);
        allEntrances = entranceService.findAllEntrances();
    }

    public void applyFilters() {
        filteredStates = allStates.stream()
                .filter(s -> (selectedEmployee == null || s.getEmployee().getId() == selectedEmployee.getId()))
                .filter(s -> selectedEntrance == null || s.getEntrance().getId() == selectedEntrance.getId())
                .filter(s -> locationStateFilter == null || s.getCurrentState() == locationStateFilter)
                .collect(Collectors.toList());
    }

    public void prepareReset(EmployeeEntranceState state) {
        this.selectedState = state;
        this.newState = null;
        this.resetReason = "";
    }

    public void confirmReset() {
        try {
            if (selectedState != null && newState != null) {
                LocationState oldLocationState = selectedState.getCurrentState();

                LocationState newLocationState = LocationState.valueOf(newState);
                stateService.resetEmployeeState(
                        selectedState.getEmployee().getId(),
                        selectedState.getEntrance().getId(),
                        newLocationState,
                        getCurrentAdminUsername(), // implement this method
                        resetReason,
                        selectedState.getDeviceUsed()
                );
                String successDetail = "Successfully reset employee state from " + oldLocationState + " to " + newLocationState
                        + " for employee: " + selectedState.getEmployee().getFullName();
                auditLogService.logActivity(AuditActionType.UPDATE, "State Reset Page", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());

                refresh();
            } else {
                String details = "Selected state or new state is null.";
                auditLogService.logActivity(AuditActionType.UPDATE, "State Reset Page", ActionResult.FAILED, details, userSession.getCurrentUser());
                JSF.addErrorMessage("Error: Selected state or new state cannot be null.");
            }
        } catch (Exception e) {
            String errorDetail = "Failed to reset state for employee ID: "
                    + (selectedState != null ? selectedState.getEmployee().getId() : "Unknown")
                    + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.UPDATE, "State Reset Page", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());

            JSF.addErrorMessage("Error resetting state: " + e.getLocalizedMessage());

        }
    }

    public void export(String fileType){
    
        List<OccupantState> os = new ArrayList<>();
        
        for(EmployeeEntranceState es: filteredStates){
        OccupantState ocs = new OccupantState();
        ocs.setEntranceName(es.getEntrance().getEntranceName());
        ocs.setFullName(es.getEmployee().getFullName());
        ocs.setNationalID(es.getEmployee().getGhanaCardNumber());
        ocs.setLastUpdated(es.getFormattedLastModifiedDate());
        ocs.setPosition(es.getCurrentState().toString());
        ocs.setUpdatedBy(es.getLastUpdatedBy());
        
        os.add(ocs);
        }
        ReportOutputFileType type = ReportOutputFileType.valueOf(fileType.toUpperCase());
        rm.addParam("printedBy", userSession.getUsername());
        rm.addParam("occupantState", new JRBeanCollectionDataSource(os));
        rm.setReportFile(ReportGenerator.OCCUPANT_STATE);
        rm.setReportData(Arrays.asList(new Object()));
        rm.generateReport(type);
    }
    
    private void refresh() {
        allStates = stateService.findAllEmployeeEntranceStates();
        applyFilters();
    }

    private String getCurrentAdminUsername() {
        String username = userSession.getUsername();
        return (username != null) ? username : "ADMIN";
    }

    public List<Employee> completeEmployees(String query) {
        return employeeService.searchEmployees(query);
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setEntranceStateBreadCrumb();
    }
}
