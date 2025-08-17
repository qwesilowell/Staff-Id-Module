/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.report.model;

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
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyReport {

    public String severityLevel;
    public String anomalyType;
    public String anomalyStatus;
    public String empName;
    public String issue;
    public String entranceName;
    public String deviceName;
    public String timestamp;

}
