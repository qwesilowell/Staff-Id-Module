/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import com.margins.STIM.entity.enums.AnomalySeverity;
import com.margins.STIM.entity.enums.AnomalyStatus;
import com.margins.STIM.entity.enums.AnomalyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Table(name = "ACCESS_ANOMALIES")
@Entity
public class AccessAnomaly extends EntityModel implements Serializable {

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "anomaly_type")
    private AnomalyType anomalyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "Severity_Level")
    private AnomalySeverity  anomalySeverity;
    
    private String message;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Devices device;

    @ManyToOne
    @JoinColumn(name = "entrance_id")
    private Entrances entrance;

    @ManyToOne(optional = true)
    @JoinColumn(name = "accesslog_id")
    private AccessLog accessLog;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private AnomalyStatus anomalyStatus = AnomalyStatus.UNATTENDED;
    
    @ManyToOne
    @JoinColumn(name = "HANDLED_BY")
    private Users handledBy;
    
    private LocalDateTime resolvedAt;
}
