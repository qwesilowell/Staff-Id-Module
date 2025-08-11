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
@AllArgsConstructor
@NoArgsConstructor
public class EntranceReport {
    private String entranceName;
    private String entranceLocation;
    private long totalAccesses;
    private long uniqueUsers;
    private long grantedAccesses;      // Only granted attempts
    private long deniedAccesses;        //Only denied attempts
    private String lastAccessed;
    private String lastAccessedBy; //Employe Name
    private String lastAccessStatus;  //"GRANTED" OR "denied"
    private long employeeCount;
    private long roleCount;
}
