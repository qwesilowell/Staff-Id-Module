/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.margins.STIM.entity.model;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Darlington
 */
@Getter
@Setter


public class VerificationRequest {

    private String pinNumber;

    private String image;

    private String dataType;

    private String center;

    private String merchantKey;
    
    private String merchantCode;
    
    private String position;
    
    private String userUD;
    
    private String deviceOs;
    
    
   public VerificationRequest(){
           merchantKey = ("69af98f5-39fb-44e6-81c7-5e496328cc58");
           merchantCode =("69af98f5-39fb-44e6-81c7-5e496328cc58");
           dataType = "PNG";}
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VerificationRequest{");
        sb.append("pinNumber=").append(pinNumber);
        sb.append(", image=").append(image);
        sb.append(", dataType=").append(dataType);
        sb.append(", center=").append(center);
        sb.append(", merchantKey=").append(merchantKey);
        sb.append(", merchantCode=").append(merchantCode);
        sb.append(", position=").append(position);
        sb.append(", userUD=").append(userUD);
        sb.append(", deviceOs=").append(deviceOs);
        sb.append('}');
        return sb.toString();
    }
    
    


}
