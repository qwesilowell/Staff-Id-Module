/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.LocationState;
import com.margins.STIM.service.EmployeeEntranceStateService;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
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
        if (selectedState != null && newState != null) {
            LocationState newLocationState = LocationState.valueOf(newState);
            stateService.resetEmployeeState(
                    selectedState.getEmployee().getId(),
                    selectedState.getEntrance().getId(),
                    newLocationState,
                    getCurrentAdminUsername(), // implement this method
                    resetReason
            );
            refresh();
        }
    }

    private void refresh() {
        allStates = stateService.findAllEmployeeEntranceStates();
        applyFilters();
    }

    private String getCurrentAdminUsername() {
        String username = (String) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get("username");
        return (username != null) ? username : "ADMIN";
    }

    public List<Employee> completeEmployees(String query) {
        return employeeService.searchEmployees(query);
    }
}
