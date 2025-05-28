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
@Table(name = "Role_Time_Access")
@Getter
@Setter
public class RoleTimeAccess extends EntityModel implements Serializable {

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID", referencedColumnName = "id"  ,nullable = false)
    private EmployeeRole employeeRole;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ENTRANCE_ID",referencedColumnName = "ENTRANCE_DEVICE_ID", nullable = false)
    private Entrances entrances;

    @Temporal(TemporalType.TIME)
    private Date startTime;

    @Temporal(TemporalType.TIME)
    private Date endTime;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

}

