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
@Table(name = "SUB_OFFICE")
public class Sub_Office extends EntityModel implements Serializable {


    @Column(name = "SUB_OFFICE_NAME", nullable = false)
    private String subOfficeName;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    // Getters and Setters
   

    public String getSubOfficeName() {
        return subOfficeName;
    }

    public void setSubOfficeName(String subOfficeName) {
        this.subOfficeName = subOfficeName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    // Constructors
    public Sub_Office() {
    }

    public Sub_Office(Long subOfficeId, String subOfficeName, Department department) {
        this.subOfficeName = subOfficeName;
        this.department = department;
    }
}
