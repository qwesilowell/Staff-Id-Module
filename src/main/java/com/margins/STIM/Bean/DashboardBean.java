/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */

import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.io.IOException;

@Named("dashboard2")
@SessionScoped
public class DashboardBean implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Employee_Service employeeService;
    
    @Inject
    private EntrancesService entrancesService;

    private String adminUsername;
    private String userRole;
    private int totalEmployees;
    private long totalEntrances;

    @PostConstruct
    public void init() {
        loadUserDetails();
        loadTotalEmployees();
//        loadTotalEntrances();
    }
    
    public void logout() throws IOException {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        ec.invalidateSession(); // Ends user session
        ec.redirect(ec.getRequestContextPath() + "/login.xhtml"); // Redirects to login page
    }

    private void loadUserDetails() {
        try {
            String loggedInUserId = (String) FacesContext.getCurrentInstance()
                             .getExternalContext()
                             .getSessionMap()
                             .get("loggedInUserId");

             if (loggedInUserId != null) {
                TypedQuery<Users> query = entityManager.createQuery(
                    "SELECT u FROM Users u WHERE u.ghanaCardNumber = :id", Users.class);
                query.setParameter("id", loggedInUserId);

                List<Users> users = query.getResultList();
                if (!users.isEmpty()) {
                    Users user = users.get(0);
                    this.adminUsername = user.getUsername();
                    this.userRole = user.getUserRole();
                } else {
                    this.adminUsername = "Unknown User";
                    this.userRole = "Unknown Role";
                }
            } else {
                this.adminUsername = "Unknown User";
                this.userRole = "Unknown Role";
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.adminUsername = "Unknown User";
            this.userRole = "Unknown Role";
        }
    }

    private void loadTotalEmployees() {
        try {
            List<Employee> employees = employeeService.findAllEmployees();
            this.totalEmployees = employees.size();
        } catch (Exception e) {
            e.printStackTrace();
            this.totalEmployees = 0;
        }
    }
//    
//    private void loadTotalEntrances() {
//        try {
//            this.totalEntrances = entrancesService.countTotalEntrances();  // Fetch the total entrances
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.totalEntrances = 0;
//        }
//    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getUserRole() {
        return userRole;
    }

    public int getTotalEmployees() {
        return totalEmployees;
    }
    
    public long getTotalEntrances() {
        return totalEntrances;
    }

//    public PieChartModel getPieChartModel() {
//        return pieChartModel;
//    }
//
//    public BarChartModel getBarChartModel() {
//        return barChartModel;
//    }
}