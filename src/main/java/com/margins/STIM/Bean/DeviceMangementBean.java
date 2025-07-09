/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("deviceBean")
@ViewScoped
@Setter
@Getter
public class DeviceMangementBean implements Serializable {

    private Devices device;
    private List<Devices> devices = new ArrayList<>();
    private List<Entrances> availableEntrances = new ArrayList<>();
    private Entrances selectedEntrance;
    private String currentFilter;
    private List<Devices> filteredDevices;

    @Inject
    private DeviceService deviceService;

    @Inject
    private EntrancesService entranceService;

    @PostConstruct
    public void init() {
        devices = deviceService.getAllDevices();
        availableEntrances = entranceService.findAllEntrances();
        device = new Devices();
        currentFilter = "all";
        filterDevices("all");
    }

    public void addDevice() {
        try {
            if (!validateDeviceData()) {
                return;
            }
            assignSelectedEntrance();
            device.setDeleted(false);
            deviceService.registerDevice(device);
            devices.add(device);
            selectedEntrance = null;
            device = new Devices();
            JSF.addSuccessMessage("Device added successfully.");
        } catch (Exception e) {
            selectedEntrance = null;
            device = new Devices();
            JSF.addErrorMessage("Error adding device: " + e.getLocalizedMessage() + "");
        }
    }

    public void saveDevice() {
        try {
            if (!validateDeviceData()) {
                return;
            }

            assignSelectedEntrance();
            device.setDeleted(false);

            // Check if this is a new device or existing device
            if (isNewDevice()) {
                // This is a new device - use registerDevice (persist)
                deviceService.registerDevice(device);
                devices.add(device);
                JSF.addSuccessMessage("Device added successfully.");
            } else {
                // This is an existing device - use updateDevice (merge)
                deviceService.updateDevice(device);
                // Update the device in the list
                updateDeviceInList(device);
                JSF.addSuccessMessage("Device updated successfully.");
            }

            reset();
           filterDevices("all");
        } catch (Exception e) {
            JSF.addErrorMessage("Error saving device: " + e.getLocalizedMessage());
            e.printStackTrace(); // Add this for debugging
        }
    }

    private void updateDeviceInList(Devices updatedDevice) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getDeviceId().equals(updatedDevice.getDeviceId())) {
                devices.set(i, updatedDevice);
                break;
            }
        }
    }

    private boolean isNewDevice() {
        return device.getDeviceId() == null
                || devices.stream().noneMatch(d -> d.getDeviceId().equals(device.getDeviceId()));
    }

    public void startEdit(Devices selected) {
        this.device = selected;
        this.selectedEntrance = selected.getEntrance();
    }

    public void cancelEdit() {
        reset();
        JSF.addWarningMessage("Edit cancelled.");
    }

    // Method to check if we're in edit mode
    public boolean isEditMode() {
        return !isNewDevice();
    }

    public void assignSelectedEntrance() {
        if (selectedEntrance != null) {
            device.setEntrance(selectedEntrance);
        } else {
            JSF.addWarningMessage("No Entrance selected");
        }
    }

    public void editDevice(Devices selected) {
        this.device = selected;
        this.selectedEntrance = selected.getEntrance();
    }

    public void deleteDevice(Devices selected) {
        deviceService.deleteDevice(selected.getDeviceId());
        devices = deviceService.getAllDevices();
        JSF.addWarningMessage("Device is deleted.");
    }

    public void saveAllDevices() {
        try {
            for (Devices d : devices) {
                if (d.equals(device) && selectedEntrance != null) {
                    d.setEntrance(selectedEntrance);
                }
                deviceService.updateDevice(d);
            }
            JSF.addSuccessMessage("All Edited Devices saved.");
            reset();
        } catch (Exception e) {
            JSF.addErrorMessage("Error saving devices: " + e.getMessage());
        }
    }

    public void setDevicePosition(String position) {
        if ("ENTRY".equalsIgnoreCase(position)) {
            device.setDevicePosition(DevicePosition.ENTRY);
        } else if ("EXIT".equalsIgnoreCase(position)) {
            device.setDevicePosition(DevicePosition.EXIT);
        }
    }

    public boolean validateDeviceData() {
        boolean isValid = true;

        if (device.getDevicePosition() == null) {
            JSF.addWarningMessage("Please select a Device Position (Entry or Exit)");
            isValid = false;
        }

        if (device.getDeviceName() == null || device.getDeviceName().trim().isEmpty()) {
            JSF.addWarningMessage("Please enter a Device Name");
            isValid = false;
        }
        return isValid;
    }

    public void updateDevicePosition(Devices target, String position) {
        if ("ENTRY".equalsIgnoreCase(position)) {
            target.setDevicePosition(DevicePosition.ENTRY);
        } else if ("EXIT".equalsIgnoreCase(position)) {
            target.setDevicePosition(DevicePosition.EXIT);
        }
        deviceService.updateDevice(target);
        JSF.addSuccessMessageWithSummary("Success", "Device position updated.");
    }

    // Stats
    public int getTotalDevices() {
        return devices.size();
    }

    public long getEntryDevices() {
        return devices.stream().filter(d -> d.getDevicePosition() == DevicePosition.ENTRY).count();
    }

    public long getExitDevices() {
        return devices.stream().filter(d -> d.getDevicePosition() == DevicePosition.EXIT).count();
    }

    public long getUnassignedDevices() {
        return devices.stream().filter(d -> d.getEntrance() == null).count();
    }

    public void filterDevices(String filterType) {
        this.currentFilter = filterType;

        switch (filterType.toLowerCase()) {
            case "entry":
                filteredDevices = devices.stream()
                        .filter(d -> d.getDevicePosition() == DevicePosition.ENTRY)
                        .collect(Collectors.toList());
                break;
            case "exit":
                filteredDevices = devices.stream()
                        .filter(d -> d.getDevicePosition() == DevicePosition.EXIT)
                        .collect(Collectors.toList());
                break;
            case "unassigned":
                filteredDevices = devices.stream()
                        .filter(d -> d.getEntrance() == null)
                        .collect(Collectors.toList());
                break;
            default:
                filteredDevices = new ArrayList<>(devices);
                break;
        }
    }

    public List<Devices> getFilteredDevices() {
        if (filteredDevices == null) {
            filterDevices("all"); // Initialize with all devices
        }
        return filteredDevices;
    }

    public boolean filterActive(String filterType) {
        return currentFilter.equalsIgnoreCase(filterType);
    }

    public List<Entrances> searchEntrances(String query) {

        if (availableEntrances == null) {
            availableEntrances = entranceService.findAllEntrances();
        }

        if (query == null || query.trim().isEmpty()) {
            return availableEntrances;
        }

        List<Entrances> filtered = availableEntrances.stream()
                .filter(entrance -> entrance.getEntranceName().toLowerCase()
                .contains(query.toLowerCase())
                || entrance.getEntranceDeviceId().toLowerCase()
                        .contains(query.toLowerCase()))
                .collect(Collectors.toList());

        return filtered;
    }

    public void reset() {
        selectedEntrance = null;
        device = new Devices();
        JSF.addSuccessMessageWithSummary("Reset.....", "Form reseted.");
    }

}
