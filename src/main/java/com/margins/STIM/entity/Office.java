/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.*;

/**
 *
 * @author PhilipManteAsare
 */

@Entity
@Table(name = "Office")
public class Office extends EntityModel implements Serializable {


    @Column(name = "office_name", nullable = false)
    private String officeName;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state", nullable = false)
    private String Region;

    @Column(name = "country", nullable = false)
    private String country;

    
    
    @OneToMany(mappedBy = "office")
    private List<Department> departments; // One office can have many departments
    
    // Getters and Setters


    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return Region;
    }

    public void setState(String state) {
        this.Region = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // Constructors
    public Office() {
    }

    public Office(Long officeId, String officeName, String city, String state, String country) {
        this.officeName = officeName;
        this.city = city;
        this.Region = state;
        this.country = country;
    }
    
    
}
