/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

//import com.margins.STIM.enums.EntityModel;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 *
 * @author PhilipManteAsare
 */

@Entity
@Table(name = "ADMIN_USER")
@Cacheable(false)
public class Users /*extends EntityModel*/ implements Serializable {

    @Id
    @Column(name = "ADMINGHANA_CARD_NUMBER ", nullable = false, unique = true, length = 15)
    private String ghanaCardNumber;
   
    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "USER_ROLE", nullable = false)
    private String userRole;


  
    

    // Getters and Setters
    
    public String getGhanaCardNumber() {
        return ghanaCardNumber;
    }

    public void setGhanaCardNumber(String ghanaCardNumber) {
        this.ghanaCardNumber = ghanaCardNumber;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }




    // Constructors
    public Users() {
    }

    
    public Users(String ghanaCardNumber,String username, String userRole, String password, LocalDateTime createdAt) {
        this.username = username;
        this.userRole = userRole;
        this.ghanaCardNumber = ghanaCardNumber;
    }
}
