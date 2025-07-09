/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.*;

/**
 * Entity class for Employee Roles
 * Represents the roles assigned to employees.
 * 
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "Employee_roles")
@Cacheable(false)
public class EmployeeRole extends EntityModel implements Serializable {


    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @Column(name = "role_description")
    private String roleDescription;
    
    @OneToMany(mappedBy = "role")
    private List<Employee> employees;
    
    @ManyToMany
    @JoinTable(
            name = "role_entrance_access",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "entrance_device_id")
    )   
    private Set<Entrances> accessibleEntrances = new HashSet<>();
    


    // Getters and Setters


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    public void setRoleDescription(String roleDescription) {
        this.roleDescription = roleDescription;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public Set<Entrances> getAccessibleEntrances() {
        return accessibleEntrances;
    }

    public void setAccessibleEntrances(Set<Entrances> aceessibleEntrances) {
        this.accessibleEntrances = aceessibleEntrances;
    }   

    // Constructors
    public EmployeeRole() {
    }

    public EmployeeRole(int roleId, String roleName, String roleDescription) {
    
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }
    
    @Override
    public String toString() {
        return this.roleName;
    }

}

//    @Override
//    public String toString() {
//        return "EmployeeRole{" + "roleName=" + roleName + ", roleDescription=" + roleDescription + ", employees=" + employees + '}';
//    }

