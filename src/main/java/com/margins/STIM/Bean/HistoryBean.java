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
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
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

    private List<AccessLog> recentAccessAttempts;
    private LazyDataModel<AccessLog> lazyAccessLogs;
    private List<AccessLog> filteredAccessAttempts;
    private List<String> allResults;
    private LocalDate selectedDate;

    //Filters
    private String employeeName;
    private String ghanaCardNumber;
    private String selectedEntranceId;
    private List<Entrances> allEntrances;
    private String employeeId;
    private String entranceId;
    private String result;

    private List<LocalDateTime> timeRange;

    private LocalDateTime endTime;

    @PostConstruct
    public void init() {

        allEntrances = entrancesService.findAllEntrances();
        allResults = Arrays.asList("granted", "denied");
        recentAccessAttempts = accessLogService.getRecentAccessAttempts(30);
    }

    public void fetchCriteria() {
        recentAccessAttempts = accessLogService.filterAccessLog(timeRange, selectedEntranceId, ghanaCardNumber, employeeName, result);
    }
    
    public void reset(){
    timeRange= null;
    result = null;
    selectedEntranceId = null;
    ghanaCardNumber="";
    employeeName=""; 
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
