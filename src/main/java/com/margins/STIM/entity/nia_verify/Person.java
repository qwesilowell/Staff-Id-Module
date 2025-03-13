/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.nia_verify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author ernest
 */
@Getter
@Setter
public class Person implements Serializable{
    public String nationalId;
    public Date cardValidTo;
    public String surname;
    public String forenames;
    public String nationality;
    public Date birthDate;
    public ArrayList<Address> addresses;
    public BiometricFeed biometricFeed; 
    public String gender;
}
