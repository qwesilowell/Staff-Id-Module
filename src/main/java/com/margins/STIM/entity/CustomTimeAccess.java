/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table (name = "CUSTOM_TIME_ACCESS")
@Getter
@Setter
public class CustomTimeAccess extends EntityModel implements Serializable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMPLOYEE_ID", referencedColumnName = "id", nullable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENTRANCE_ID", referencedColumnName = "id", nullable = false )
    private Entrances entrances;
    
    @Column(name = "START_TIME")
    @Temporal(TemporalType.TIME)
    private Date startTime;
    
    @Column(name = "END_TIME")
    @Temporal(TemporalType.TIME)
    private Date endTime;   
    
    @Column(name = "DAY_OF_WEEK")
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;
    
    @Column(name = "ISREVOKED")
    private boolean isRevoked = false;
}
