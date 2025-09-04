/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.DTO;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
public class VisitorCreateRequest {

    private String forenames;
    private String surname;
    private String gender;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String nationalId;
    private String email;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DayOfWeek dayOfWeek;
    private List<Integer> entranceIds;
}
