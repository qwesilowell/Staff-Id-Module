/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Entity
@Table(name = "Department")
public class Department extends EntityModel implements Serializable {

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "department_head")
    private String departmentHead;
}
