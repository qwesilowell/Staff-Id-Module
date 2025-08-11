/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.DTO;

import com.margins.STIM.util.DateFormatter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter                                                 
public class AccessHistoryDTO {

    private String employeeName;
    private String ghanaCardNumber;
    private String entranceName;
    private String deviceEntryName;
    private String deviceExitName;
    private LocalDate date;

    private LocalDateTime timeEntered;
    private LocalDateTime timeExited;

    private String entryResult;
    private String exitResult;
    
    private Long entryLogId;
    private Long exitLogId;

//Access duration if both entry and exit are available
    public Duration getDuration() {
        return (timeEntered != null && timeExited != null)
                ? Duration.between(timeEntered, timeExited)
                : null;

    }
    
    public String getFormattedTimeEntered() {
        return DateFormatter.formatDateTime(timeEntered);
    }

    public String getFormattedTimeExited() {
        return DateFormatter.formatDateTime(timeExited);
    }
}
