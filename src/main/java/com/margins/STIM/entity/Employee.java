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
import java.util.Date;

@Entity
@Table(name = "Employee")
public class Employee implements Serializable {

    @Id
    @Column(name = "GHANA_CARD_NUMBER", nullable = false, unique = true, length = 15)
    private String ghanaCardNumber;
    
    @Column(name = "FIRST_NAME",nullable = false)
    private String firstname;

    @Column(name = "LAST_NAME",nullable = false)
    private String lastname;

    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "GENDER", nullable = false)
    private String gender;

    @Column(name = "ADDRESS", nullable = false)
    private String address;
    
    @Column(name = "EMAIL", nullable = false)
    private String email;

//    @ManyToOne
//    @JoinColumn(name = "department_id", nullable = false)
//    private Department department;
//
    @OneToOne(mappedBy = "employee", cascade = CascadeType.ALL) 
    private BiometricData biometricData;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = true)
    private EmployeeRole role;
    
    
//    @ManyToMany
//    @JoinTable(
//            name = "employee_roles_mapping",
//            joinColumns = @JoinColumn(name = "employee_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id")
//    )
//    private List<EmployeeRole> roles;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
//
//    @ManyToOne
//    @JoinColumn(name = "UsersGhana_card_number", nullable = false)
////    private Users user;
//
//    @ManyToOne
//    @JoinColumn(name = "office_id", nullable = false)
//    private Office office;
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
    

//    public Department getDepartment() {
//        return department;
//    }
//
//    public void setDepartment(Department department) {
//        this.department = department;
//    }
//
//    public byte[] getEmployeePhoto() {
//        return employeePhoto;
//    }
//
//    public void setEmployeePhoto(byte[] employeePhoto) {
//        this.employeePhoto = employeePhoto;
//    }
//
    public BiometricData getBiometricData() {
        return biometricData;
    }
//
    public void setBiometricData(BiometricData biometricData) {
        this.biometricData = biometricData;
    }
    
//    public List<EmployeeRole> getRoles() {
//        return roles;
//    }
//
//    public void setRoles(List<EmployeeRole> roles) {
//        this.roles = roles;
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
    public  void setCreatedAt() {
        Date newDate= new Date();
        this.createdAt = newDate;
    }

//    public Users getUser() {
//        return user;
//    }
//
//    public void setUser(Users user) {
//        this.user = user;
//    }
//
//    public Office getOffice() {
//        return office;
//    }
//
//    public void setOffice(Office office) {
//        this.office = office;
//    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    

    public Employee(String ghanaCardNumber,String firstname, String lastname,String email, LocalDate dateOfBirth, String gender, String address, Department department, byte[] employeePhoto, BiometricData biometricData, EmployeeRole role, Date createdAt, Users user, Office office) {
        this.ghanaCardNumber = ghanaCardNumber;
        this.firstname = firstname;
        this.lastname = lastname;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.address = address;
//        this.department = department;
//        this.employeePhoto = employeePhoto;
//        this.biometricData = biometricData;
        this.role = role;
        this.createdAt = createdAt;
//        this.user = user;
//        this.office = office;
        this.email= email;
    }

//    public Employee(List<EmployeeRole> roles) {
//        this.roles = roles;
//    }

    

    public Employee() {
    }
    
    
}
