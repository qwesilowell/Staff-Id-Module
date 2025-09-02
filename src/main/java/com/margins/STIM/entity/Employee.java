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
@Getter
@Setter
public class Employee implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

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

    @Column(name = "PRIMARY_PHONE", nullable = false)
    private String primaryPhone;

    @Column(name = "SECONDARY_PHONE")
    private String secondaryPhone;

    @ManyToOne
    @JoinColumn(name = "EMPLOYMENT_STATUS_ID")
    private EmploymentStatus employmentStatus;

//    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL)
//    private BiometricData biometricData;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private EmployeeRole role;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<CustomTimeAccess> customAccessList = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
            name = "employee_entrance_access",
            joinColumns = @JoinColumn(name = "EMPLOYEE_ID"),
            inverseJoinColumns = @JoinColumn(name = "ENTRANCE_ID")
    )
    private List<Entrances> customEntrances = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public String getFullName() {
        return firstname + " " + lastname;
    }

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = new Date();
    }

    public Employee() {
    }

}
