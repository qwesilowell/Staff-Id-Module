/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import jakarta.persistence.*;

/**
 *
 * @author PhilipManteAsare
 */

@Entity
@Table(name = "Department")
public class Department extends EntityModel implements Serializable {



    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "department_head")
    private String departmentHead;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;  // One department belongs to one office


    // Getters and Setters


    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentHead() {
        return departmentHead;
    }

    public void setDepartmentHead(String departmentHead) {
        this.departmentHead = departmentHead;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    // Constructors
    public Department() {
    }

    public Department(Long departmentId, String departmentName, String departmentHead, Office office) {
        this.departmentName = departmentName;
        this.departmentHead = departmentHead;
        this.office = office;
    }
}
