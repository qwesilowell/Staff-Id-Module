/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Interface;

import com.margins.STIM.entity.Devices;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
public interface DeviceService {

    void registerDevice(Devices device);

    void updateDevice(Devices updatedDevice);
    
    Devices findDeviceById(int id);

    List<Devices> getAllDevices();

    List<Devices> getDevicesByEntrance(int id);
    
    List<Devices> searchDevices(String query);

    void deleteDevice(String deviceId);
}
