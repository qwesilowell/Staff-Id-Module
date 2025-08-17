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
public class EntranceAccessReport {

    private String employeeName;
    private String ghanaCardNumber;
    private String entranceName;
    private String deviceEntryName;
    private String deviceExitName;
    private String entryResult;
    private String exitResult;
    private String timeEntered;
    private String timeExited;
    private String status;
    private String duration;

}
