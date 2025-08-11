/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.EmploymentStatus;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.Employee_Service;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityNotFoundException;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Named("employmentStatusBean")
@ViewScoped
public class EmploymentStatusBean implements Serializable {

    private List<EmploymentStatus> employmentStatuses;
    private EmploymentStatus selectedEmploymentStatus;
    @Inject
    private BreadcrumbBean breadcrumbBean;

    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;

    @Inject
    private Employee_Service employeeService;

    @PostConstruct
    public void init() {
        refreshEmploymentStatuses();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setEmployeeStatusBreadcrumb();
    }

    public void prepareNewEmploymentStatus(ActionEvent event) {
        selectedEmploymentStatus = new EmploymentStatus();
    }

    public void refreshEmploymentStatuses() {
        employmentStatuses = employeeService.findAllEmploymentStatuses();
    }

    public void saveEmploymentStatus(ActionEvent event) {
        try {
            if (selectedEmploymentStatus.getId() == 0) { // New status (id is 0 since it's not set yet)
                employeeService.saveEmploymentStatus(selectedEmploymentStatus);
                String details = "New Role (" + selectedEmploymentStatus.getEmpstatus() + ") Created Succesfully.";
                auditLogService.logActivity(AuditActionType.CREATE, "Employee Status Page", ActionResult.SUCCESS, details, userSession.getCurrentUser());

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employment status created."));
            } else { // Existing status
                employeeService.updateEmploymentStatus(selectedEmploymentStatus.getId(), selectedEmploymentStatus);
                String details = "Role (" + selectedEmploymentStatus.getEmpstatus() + ") updated Succesfully.";
                auditLogService.logActivity(AuditActionType.UPDATE, "Employee Status Page", ActionResult.SUCCESS, details, userSession.getCurrentUser());
                
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employment status updated."));
            }
            refreshEmploymentStatuses();
        } catch (EntityNotFoundException e) {
            String details = "Role (" + selectedEmploymentStatus.getEmpstatus() + ") creating failed "+ " reason " +e.getMessage() ;
            auditLogService.logActivity(AuditActionType.CREATE, "Employee Status Page", ActionResult.FAILED, details, userSession.getCurrentUser());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Status not found."));
        } catch (Exception e) {
            String details = "Role (" + selectedEmploymentStatus.getEmpstatus() + ") creating failed " + " reason " + e.getMessage();
            auditLogService.logActivity(AuditActionType.CREATE, "Employee Status Page", ActionResult.FAILED, details, userSession.getCurrentUser());

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save status: " + e.getMessage()));
        }
    }

    public void deleteEmploymentStatus(ActionEvent event) {
        if (selectedEmploymentStatus != null) {
            try {
                employeeService.deleteEmploymentStatus(selectedEmploymentStatus.getId());
                String details = "Role (" + selectedEmploymentStatus.getEmpstatus() + ") deleted succesfully. ";
                auditLogService.logActivity(AuditActionType.DELETE, "Employee Status Page", ActionResult.SUCCESS, details, userSession.getCurrentUser());

                refreshEmploymentStatuses();
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Employment status deleted."));
            } catch (IllegalStateException e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Cannot delete status in use by employees."));
            } catch (EntityNotFoundException e) {
                
                String details = "Role (" + selectedEmploymentStatus.getEmpstatus() + ") deletion failed " + " reason " + e.getMessage();
                auditLogService.logActivity(AuditActionType.DELETE, "Employee Status Page", ActionResult.FAILED, details, userSession.getCurrentUser());

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Status not found."));
            }
        }
    }

    // Getters and Setters
    public List<EmploymentStatus> getEmploymentStatuses() {
        return employmentStatuses;
    }

    public void setEmploymentStatuses(List<EmploymentStatus> employmentStatuses) {
        this.employmentStatuses = employmentStatuses;
    }

    public EmploymentStatus getSelectedEmploymentStatus() {
        return selectedEmploymentStatus;
    }

    public void setSelectedEmploymentStatus(EmploymentStatus selectedEmploymentStatus) {
        this.selectedEmploymentStatus = selectedEmploymentStatus;
    }
}
