/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.report.model;

import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.util.DateFormatter;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserReport {
    
    private Integer userId;
    private String userName;
    private String userRole;
    private String dateCreated;
    private String userType;
    private String userStatus;
    private String accessiblePages; // New field
    
    // Constructor that converts list to string
    public UserReport(Users user, Set<ViewPermission> pages) {
        this.userId = user.getId();
        this.userName = user.getUsername();
        this.userRole = user.getUserRole().getUserRolename(); // Add other field mappings
        this.dateCreated = DateFormatter.formatDate(user.getCreatedAt()) ; // Format as needed
        this.userType = user.getUserType().toString();
        this.userStatus = user.getStatus().toString();

        this.accessiblePages = (pages != null && !pages.isEmpty())
                ? pages.stream()
                        .map(page -> "• " + page)
                        .collect(Collectors.joining("\n")) : "No Access";
    }
}
