/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.DTO.EntranceAccessStatsDTO;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.util.JSF;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Named("dashboard")
@SessionScoped
public class DashboardController implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Inject
    private AccessLogService accessLogService;

    public void logout() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession(); // Ends user session
        ec.redirect(ec.getRequestContextPath() + "/login.xhtml"); // Redirects to login page
    }

    public void tryResults() {
        try {
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime sevenDaysAgo = today.minusDays(40);
            
           List <EntranceAccessStatsDTO> message = accessLogService.getTop5EntranceAccessStats(sevenDaysAgo,today);
            if (message != null ){
                System.out.println("Results >>>>>>>>>>>>>> " + message);
            JSF.addSuccessMessage("WORKED");
            }else{
                JSF.addErrorMessage("Failed Charle");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log to server console
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to fetch results."));
        }

    }
}
