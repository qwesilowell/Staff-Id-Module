/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.nia_verify;

import java.io.Serializable;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author ernest
 */
@Getter
@Setter
public class Data implements Serializable {

    public String transactionGuid;
    public String shortGuid;
    public Date requestTimestamp;
    public Date responseTimestamp;
    public String verified;
    public boolean isException;
    public String source;
    public Person person;
    private String merchantCode;
    private String merchantName;
}
