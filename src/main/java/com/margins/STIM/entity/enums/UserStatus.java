/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.margins.STIM.entity.enums;

/**
 *
 * @author PhilipManteAsare
 */
public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING_PASSWORD_CHANGE("Pending Password Change");
    
    
    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
