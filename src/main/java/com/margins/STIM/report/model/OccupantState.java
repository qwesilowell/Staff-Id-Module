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
public class OccupantState {

    private String fullName;
    private String nationalID;
    private String entranceName;
    private String position;
    private String lastUpdated;
    private String updatedBy;
}
