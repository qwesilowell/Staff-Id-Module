/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import com.margins.STIM.entity.enums.EntranceMode;
import com.margins.STIM.entity.enums.LocationState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "employee_entrance_state_log")
@Getter
@Setter
public class EmployeeEntranceStateLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    private Entrances entrance;

    @Enumerated(EnumType.STRING)
    private LocationState state; // INSIDE or OUTSIDE

    private LocalDateTime createdDate;

    private String updatedBy;

    private String reason;

    @Enumerated(EnumType.STRING)
    private EntranceMode mode; // STRICT, LENIENT, etc.

    @ManyToOne
    private Devices device;
}
