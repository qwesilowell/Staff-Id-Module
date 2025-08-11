/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.report.model;

import java.io.InputStream;
import javax.management.relation.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeRoleReport {

    private InputStream employeeRoleCount;
    private InputStream accessibleEntrancePerRole;
    private String roleName;
    private Integer empInRoleCount;
    private Integer roleAccessibleCount;
    private String dateCreated;
    private Integer count;
    private Integer entCount;
    private Role role;
}
