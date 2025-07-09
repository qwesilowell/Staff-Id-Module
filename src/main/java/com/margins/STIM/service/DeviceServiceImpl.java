/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.Devices;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class DeviceServiceImpl implements DeviceService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void registerDevice(Devices device) {
        if (device == null || device.getDeviceId() == null) {
            throw new IllegalArgumentException("deviceId is null");
        }
        List<Devices> existingList = em.createQuery(
                "SELECT d FROM Devices d WHERE d.deviceId = :deviceId", Devices.class)
                .setParameter("deviceId", device.getDeviceId())
                .getResultList();
        if (!existingList.isEmpty()) {
            System.out.println("ERROR: Device already exists in database!");
            throw new IllegalStateException("Device " + device.getDeviceName() +" with ID " + device.getDeviceId()+ " already exists. Use update instead.");
        }

        try {
            em.persist(device);
            System.out.println("Device persisted successfully");
        } catch (Exception e) {
            System.out.println("ERROR persisting device: " + e.getMessage());
            throw e;
        }
        System.out.println("=== END DEBUG registerDevice ===");
    }

    @Override
    public void updateDevice(Devices updatedDevice) {
        if (updatedDevice == null || updatedDevice.getDeviceId() == null) {
            throw new IllegalArgumentException("deviceId is null");
        }

        Devices existingDevice = em.find(Devices.class, updatedDevice.getDeviceId());
        if (existingDevice != null && !existingDevice.isDeleted()) {
            existingDevice.setDeviceName(updatedDevice.getDeviceName());
            existingDevice.setDevicePosition(updatedDevice.getDevicePosition());
            existingDevice.setEntrance(updatedDevice.getEntrance());
            try {
                em.merge(existingDevice);
                System.out.println("Device merged successfully");
            } catch (Exception e) {
                System.out.println("ERROR merging device: " + e.getMessage());
                throw e;
            }
        } else {
            System.out.println("ERROR: Device not found or deleted");
            throw new EntityNotFoundException("Device not found or deleted");
        }
        System.out.println("=== END DEBUG updateDevice ===");
    }

    
    @Override
    public List<Devices> getAllDevices() {
        return em.createQuery("SELECT d FROM Devices d WHERE d.deleted = false", Devices.class)
                .getResultList();
    }

    @Override
    public Devices findDeviceById(int id) {
        Devices device = em.find(Devices.class, id);
        return (device != null && !device.isDeleted()) ? device : null;
    }

    @Override
    public List<Devices> getDevicesByEntrance(int id) {
        return em.createQuery("SELECT d FROM Devices d WHERE d.entrance.id = :id AND d.deleted = false", Devices.class)
                .setParameter("id", id)
                .getResultList();
    }
    
    @Override
    public List<Devices> searchDevices(String query) {
        return em.createQuery(
                "SELECT d FROM Devices d "
                + "WHERE (LOWER(d.deviceName) LIKE :query OR LOWER(d.deviceId) LIKE :query) "
                + "AND d.deleted = false", Devices.class)
                .setParameter("query", "%" + query.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public void deleteDevice(String deviceId) {
        Devices device = em.find(Devices.class, deviceId);
        if (device != null) {
            device.setDeleted(true);
            em.merge(device); // update instead of remove
        }       
    }
}
