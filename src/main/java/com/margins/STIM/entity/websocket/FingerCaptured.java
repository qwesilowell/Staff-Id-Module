/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.websocket;

import com.margins.STIM.util.FingerprintProcessor;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Philip
 */
@Getter
@Setter
public class FingerCaptured implements Serializable {

    private String dataType;
    
    private String position;
    
    private String image;

    public FingerCaptured(String dataType, String position, String image) {
        this.position = position;
        this.image = FingerprintProcessor.imageDpi(image);
        this.dataType = dataType;
    }

   
}
