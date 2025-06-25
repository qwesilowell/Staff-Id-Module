/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Entity
@Table(name = "access_log")
public class AccessLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "GHANA_CARD_NUMBER")
    private Employee employee; // Ghana Card number

    @Column(name = "entrance_id")
    private String entranceId; // entrance_Device_ID

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "result")
    private String result; // "granted" or "denied"

    @Column(name = "verification_time") //, columnDefinition = "NUMBER(2,2) DEFAULT 0"
    private Double verificationTime; // in seconds
    
    public AccessLog() {
    }

    public AccessLog(Employee employee, String entranceId, String result, Double verificationTime) {
        this.employee = employee;
        this.entranceId = entranceId;
        this.timestamp = LocalDateTime.now();
        this.result = result;
        this.verificationTime = verificationTime;
    }
}
