/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table (name="ACCESS_LEVELS")
public class Access_Levels extends EntityModel implements Serializable{
    @Column (name = "LEVEL_typE", nullable = false)
    private String level_name;
    
    @Column (name= "LEVEL_DESCRIPTION", nullable = false)
    private String level_desc;
    
    @Column(name = "IS_ALLOWED", nullable = false)
    private boolean isAllowed; // Determines if access is granted

    //Getters and Setters

    public String getLevel_name() {
        return level_name;
    }

    public void setLevel_name(String level_name) {
        this.level_name = level_name;
    }

    public String getLevel_desc() {
        return level_desc;
    }

    public void setLevel_desc(String level_desc) {
        this.level_desc = level_desc;
    }

    public boolean getIsAllowed() {
        return isAllowed;
    }

    public void setIsAllowed(boolean isAllowed) {
        this.isAllowed = isAllowed;
    }
    
    
    
    //Constructer

    public Access_Levels(String level_name, String level_desc,boolean isAllowed) {
        this.level_name = level_name;
        this.level_desc = level_desc;
        this.isAllowed = isAllowed;
    }
  
    public Access_Levels() {
    }
    
}


