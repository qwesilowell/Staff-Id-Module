/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.websocket;

import com.margins.STIM.entity.EntityModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author MalickMoro-Samah
 */
@Getter
@Setter
@Entity
@Table(name = "FINGER_CAPTURED")
public class FingerCaptured extends EntityModel {

    @Column(name = "CAPTURE_CODE")
    private String captureCode;
    
    @Column(name = "POSITION")
    private String position;
    
    @Column(name = "IMAGE")
    private byte[] image;
}
