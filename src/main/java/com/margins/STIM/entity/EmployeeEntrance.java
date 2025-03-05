/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "Employee_Entrances")
public class EmployeeEntrance implements Serializable  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Employee_GhanaCard_number", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "Entrance_Device_id", nullable = false)
    private Entrances entrance;

    @ManyToOne
    @JoinColumn(name = "Access_level_id", nullable = false)
    private Access_Levels accessLevel;
    
    //Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Entrances getEntrance() {
        return entrance;
    }

    public void setEntrance(Entrances entrance) {
        this.entrance = entrance;
    }

    public Access_Levels getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Access_Levels accessLevel) {
        this.accessLevel = accessLevel;
    }

    // Constructor for NEW entities (no ID needed)
    public EmployeeEntrance(Employee employee, Entrances entrance, Access_Levels accessLevel) {
        this.employee = employee;
        this.entrance = entrance;
        this.accessLevel = accessLevel;
    }
    
    // Constructor for EXISTING entities (with ID)
    public EmployeeEntrance(Long id, Employee employee, Entrances entrance, Access_Levels accessLevel) {
        this.id = id;
        this.employee = employee;
        this.entrance = entrance;
        this.accessLevel = accessLevel;
    }

    public EmployeeEntrance() {
    }
    
    
}