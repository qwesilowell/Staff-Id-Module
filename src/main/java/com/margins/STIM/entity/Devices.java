/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import com.margins.STIM.entity.enums.DevicePosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Table(name = "DOOR_DEVICE_MANAGEMENT")
@Entity
@Getter
@Setter
public class Devices extends EntityModel implements Serializable {

    @Column(name = "device_id", nullable = false, unique = true)
    private String deviceId; // Use device serial, MAC, or SSID as unique ID

    @Column(name = "device_name", nullable = false)
    private String deviceName; // Friendly name like "Main Gate Entry"

    @Enumerated(EnumType.STRING)
    @Column(name = "device_position", nullable = false)
    private DevicePosition devicePosition; // ENTRY or EXIT

    @ManyToOne(optional = true)
    @JoinColumn(name = "entrance_device_id", referencedColumnName = "ENTRANCE_DEVICE_ID")
    private Entrances entrance; // The entrance this device belongs to

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    public Devices() {
    }

}
