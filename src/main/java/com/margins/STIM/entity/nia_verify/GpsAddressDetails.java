/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.nia_verify;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author ernest
 */
@Getter
@Setter
public class GpsAddressDetails implements Serializable{
    public String gpsName;
    public String region;
    public String district;
    public String area;
    public String street;
    public String longitude;
    public String latitude; 
}
