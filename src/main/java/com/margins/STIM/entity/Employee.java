/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import jakarta.persistence.Entity;

/**
 *
 * @author PhilipManteAsare
 */
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Employee")
public class Employee implements Serializable {

    @Id
    @Column(name = "GHANA_CARD_NUMBER", nullable = false, unique = true, length = 15)
    private String ghanaCardNumber;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstname;

    @Column(name = "LAST_NAME", nullable = false)
    private String lastname;

    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "GENDER", nullable = false)
    private String gender;

    @Column(name = "ADDRESS", nullable = false)
    private String address;

    @Column(name = "EMAIL")
    private String email;
    
    @Getter
    @Setter
    @Column(name = "PRIMARY_PHONE", nullable = false)
    private String primaryPhone;

    @Getter
    @Setter
    @Column(name = "SECONDARY_PHONE")
    private String secondaryPhone;
    
    
    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "EMPLOYMENT_STATUS_ID")
    private EmploymentStatus employmentStatus;

//    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
//    private BiometricData biometricData;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = true)
    private EmployeeRole role;

    @Getter
    @Setter
    @ManyToMany
    @JoinTable(
            name = "employee_entrance_access",
            joinColumns = @JoinColumn(name = "GHANA_CARD_NUMBER"),
            inverseJoinColumns = @JoinColumn(name = "entrance_device_id")
    )
    private List<Entrances> customEntrances = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    
    public String getFullName() {
        return firstname + " " + lastname;
    }

    // Getters and Setters
    public String getGhanaCardNumber() {
        return ghanaCardNumber;
    }

    public void setGhanaCardNumber(String ghanaCardNumber) {
        this.ghanaCardNumber = ghanaCardNumber;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

//    public BiometricData getBiometricData() {
//        return biometricData;
//    }
//
//    public void setBiometricData(BiometricData biometricData) {
//        this.biometricData = biometricData;
//    }

    public EmployeeRole getRole() {
        return role;
    }

    public void setRole(EmployeeRole role) {
        this.role = role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void setCreatedAt() {
        Date newDate = new Date();
        this.createdAt = newDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Employee() {
    }

}
