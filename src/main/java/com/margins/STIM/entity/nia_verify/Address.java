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
public class Address implements Serializable{
    public String type;
    public String community;
    public String postalCode;
    public String town;
    public String countryName;
    public String districtName;
    public String region;
    public String addressDigital;
    public GpsAddressDetails gpsAddressDetails;
}
