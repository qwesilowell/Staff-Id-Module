/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import java.io.Serializable;

/**
 *
 * @author PhilipManteAsare
 */
public class RoleCount implements Serializable {

    private String roleName;
    private int count;

    public RoleCount(String roleName, int count) {
        this.roleName = roleName;
        this.count = count;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
