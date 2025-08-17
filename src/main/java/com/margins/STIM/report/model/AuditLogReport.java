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
public class AuditLogReport {
    private Long id;
    private String action;
    private String details;
    private String page;
    private String result;
    private String performedBy;
    private String timestamp;
}
