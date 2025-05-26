/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "ENTRANCES")
public class Entrances /*extends EntityModel*/ implements Serializable {

    @Id
    @Column(name = "ENTRANCE_DEVICE_ID", nullable = false, unique = true)
    private String entranceDeviceId;

    @Column(name = "ENTRANCE_NAME", nullable = false, unique = true)
    private String entrance_Name;

    @Column(name = "ENTRANCE_LOCATION", nullable = false)
    private String entrance_Location;

    @ManyToMany(mappedBy = "accessibleEntrances")
    private Set<EmployeeRole> allowedRoles = new HashSet<>();

    @ManyToMany(mappedBy = "customEntrances")
    private List<Employee> employees = new ArrayList<>();

    //Getters And Setters
    public String getEntrance_Device_ID() {
        return entranceDeviceId;
    }

    public void setEntrance_Device_ID(String entrance_Device_ID) {
        this.entranceDeviceId = entrance_Device_ID;
    }

    public String getEntrance_Name() {
        return entrance_Name;
    }

    public void setEntrance_Name(String entrance_Name) {
        this.entrance_Name = entrance_Name;
    }

    public String getEntrance_Location() {
        return entrance_Location;
    }

    public void setEntrance_Location(String entrance_Location) {
        this.entrance_Location = entrance_Location;
    }

    public Set<EmployeeRole> getAllowedRoles() {
        return allowedRoles;
    }

    public void setAllowedRoles(Set<EmployeeRole> allowedRoles) {
        this.allowedRoles = allowedRoles;
    }

    public Entrances(String entrance_Device_ID, String entrance_Name, String entrance_Location) {
        this.entranceDeviceId = entrance_Device_ID;
        this.entrance_Name = entrance_Name;
        this.entrance_Location = entrance_Location;
    }

    public Entrances() {
    }
}
