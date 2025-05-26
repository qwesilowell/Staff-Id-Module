/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */


@Entity
@Table(name = "time_access_rules")
public class TimeAccessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Getter
    @Setter
    private Long id;
    
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "role_id")
    private EmployeeRole role;
    
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "employee_ghanacard")
    private Employee employee;
    
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "entrance_id", nullable = false)
    private Entrances entrance;
    
    @Getter
    @Setter
    @Column(name = "start_time", nullable = false)
    private Date startTime;

    @Getter
    @Setter
    @Column(name = "end_time", nullable = false)
    private Date endTime;

    @Getter
    @Setter
    @Column(name = "days_of_week", nullable = false)
    private String daysOfWeek;
}

