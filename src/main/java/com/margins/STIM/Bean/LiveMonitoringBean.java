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
import java.io.Serializable;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Named("liveMonitoring")
@ViewScoped
@Getter
@Setter
public class LiveMonitoringBean implements Serializable {

    @EJB
    private AccessLogService accessLogService;
    @EJB
    private EntrancesService entrancesService;
    @EJB
    private Employee_Service employeeService;
    
    @Inject
    private BreadcrumbBean breadcrumbBean;

    @PostConstruct
    public void init() {

    }

    public List<AccessLog> getRecentAccessAttempts() {
        return accessLogService.getRecentAccessAttempts(20);
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setLiveMonitoringBreadcrumb();
    }
    
    
//Can be set to show as visitor
    public String getEmployeeName(Employee employee) {
        if (employee == null) {
            return "Unknown Employee";
        }
        return employee.getFullName();
    }

    public String getEntranceName(int entranceId) {
        Entrances entrance = entrancesService.findEntranceById(entranceId);
        return entrance != null ? entrance.getEntranceName(): "Entrance not found";
    }

    public String getFormattedTimestamp(AccessLog log) {
        return DateFormatter.formatDateTime(log.getTimestamp());
    }

}
