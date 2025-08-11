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
public class EntranceAccessStatsDTO {

    private Entrances entrance;
    private int entryCount;
    private int exitCount;

    public EntranceAccessStatsDTO(Entrances entrance, int entryCount, int exitCount) {
        this.entrance = entrance;
        this.entryCount = entryCount;
        this.exitCount = exitCount;
    }
    
    @Override
    public String toString() {
        return "EntranceAccessStatsDTO{"
                + "entranceName='" + entrance.getEntranceName()+ '\''
                + ", entryCounts=" + entryCount
                + ", exitCounts=" + exitCount
                + '}';
    }

}
