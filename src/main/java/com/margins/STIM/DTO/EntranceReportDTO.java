/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.DTO;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Entrances;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntranceReportDTO {
     
    private Entrances entrances;
    private long totalAccesses;
    private long uniqueUsers;
    private long grantedAccesses;      // Only granted attempts
    private long deniedAccesses;        //Only denied attempts
    private LocalDateTime lastAccessed; 
    private String lastAccessedBy; //Employe Name
    private String lastAccessStatus;  //"GRANTED" OR "denied"
    private long employeeCount;
    private long roleCount;
    private String deviceUsed;
    private  List <Devices>  entrydevice;
    private List <Devices> exitdevice;
    
}
