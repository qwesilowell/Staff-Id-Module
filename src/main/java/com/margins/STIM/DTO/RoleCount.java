/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.DTO;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
public class RoleCount implements Serializable {

    private String roleName;
    private int count;

    public RoleCount(String roleName, int count) {
        this.roleName = roleName;
        this.count = count;
    }
}
