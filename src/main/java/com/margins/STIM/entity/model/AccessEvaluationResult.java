/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.model;

import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.AnomalyType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
public class AccessEvaluationResult {

    private boolean granted;
    private List <AnomalyType> anomalies = new ArrayList<>();
    private String result;
    private String message;
    
    private Employee employee;
    private Devices device;
    private Entrances entrance;
    private LocalDateTime timestamp = LocalDateTime.now();

    public void addAnomaly(AnomalyType anomaly) {
        anomalies.add(anomaly);
    }
}
