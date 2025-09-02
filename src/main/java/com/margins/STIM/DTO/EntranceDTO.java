/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.DTO;

import com.margins.STIM.entity.Entrances;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
public class EntranceDTO {

    private int id;
    private String entranceName;
    private String entranceLocation;
    private String entranceId;

    public EntranceDTO(Entrances e) {
        this.id = e.getId();
        this.entranceName = e.getEntranceName();
        this.entranceLocation = e.getEntranceLocation();
        this.entranceId = e.getEntranceId();
    }
    
}
