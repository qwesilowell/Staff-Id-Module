/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.DTO;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
public class VisitorResponse {
    
    private int id;
    private String forenames;
    private String surname;
    private String nationalId;
    private String phoneNumber;
    private String email;

    // instead of raw VisitorAccess, just send entrance IDs or names
    private List<VisitorAccessDTO> accessList; 
}
