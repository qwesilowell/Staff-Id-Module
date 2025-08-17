/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.service.AuditLogService;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.util.JSF;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    private String searchQuery = "";
    private Entrances selectedEntranceFilter;
    private String selectedPositionFilter = "";

    @Inject
    private DeviceService deviceService;
    @Inject
    private BreadcrumbBean breadcrumbBean;
    @Inject
    private AuditLogService auditLogService;
    @Inject
    private UserSession userSession;
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

//    public void saveDevice() {
//        try {
//            if (!validateDeviceData()) {
//                String details = "Device data validation failed.";
//                auditLogService.logActivity(isNewDevice() ? AuditActionType.CREATE : AuditActionType.UPDATE, "Manage Devices", ActionResult.FAILED, details, userSession.getCurrentUser());
//                return;
//            }
//
//            assignSelectedEntrance();
//            device.setDeleted(false);
//
//            // Check if this is a new device or existing device
//            if (isNewDevice()) {
//                // This is a new device - use registerDevice (persist)
//                deviceService.registerDevice(device);
//                devices.add(device);
//                String successDetail = "Successfully added new "+ device.getDevicePosition()+" device with ID: " + device.getId()+ " for "+ device.getEntrance().getEntranceName();
//                auditLogService.logActivity(AuditActionType.CREATE, "Manage Devices", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());
//                JSF.addSuccessMessage("Device added successfully.");
//            } else {
//                // This is an existing device - use updateDevice (merge)
//                deviceService.updateDevice(device);
//                String successDetail = "Successfully updated " + device.getDevicePosition() + " device with ID: " + device.getId() + " for "+ device.getEntrance().getEntranceName();
//                auditLogService.logActivity(AuditActionType.UPDATE, "Save Device", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());
//                // Update the device in the list
//                updateDeviceInList(device);
//                JSF.addSuccessMessage("Device updated successfully.");
//            }
//
//            reset();
//           filterDevices("all");
//        } catch (Exception e) {
//            String errorDetail = "Failed to save device with ID: " + (device.getId() != 0 ? device.getId() : "Unknown") + ". Error: " + e.getMessage();
//            auditLogService.logActivity(isNewDevice() ? AuditActionType.CREATE : AuditActionType.UPDATE, "Save Device", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
//
//            
//            JSF.addErrorMessage("Error saving device: " + e.getLocalizedMessage());
//            e.printStackTrace(); // Add this for debugging
//        }
//    }
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

//    public void deleteDevice(Devices selected) {
//        try {
//            if (selected == null || selected.getId() == 0) {
//                String details = "No device selected for deletion.";
//                auditLogService.logActivity(AuditActionType.DELETE, "Delete Device", ActionResult.FAILED, details, userSession.getCurrentUser());
//                JSF.addErrorMessage("No device selected for deletion.");
//                return;
//            }
//
//            deviceService.deleteDevice(selected.getId());
//
//            String successDetail = "Successfully deleted " + selected.getDevicePosition() + " device with ID: " + selected.getDeviceId() + " for " + selected.getEntrance().getEntranceName();
//            auditLogService.logActivity(AuditActionType.DELETE, "Delete Device", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());
//
//            devices = deviceService.getAllDevices();
//            JSF.addWarningMessage("Device is deleted.");
//        } catch (Exception e) {
//            String errorDetail = "Failed to delete device with ID: " + (selected != null ? selected.getDeviceId() : "Unknown") + ". Error: " + e.getMessage();
//            auditLogService.logActivity(AuditActionType.DELETE, "Delete Device", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
//
//            JSF.addErrorMessage("Error deleting device: " + e.getLocalizedMessage());
//        }
//    }

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

//   public void filterDevices(String filterType) {
//    this.currentFilter = filterType;
//
//    Comparator<Devices> byRecentActivityDesc = Comparator
//        .comparing(
//            d -> Optional.ofNullable(d.getUpdatedAt())
//                         .orElse(d.getCreatedAt().toInstant()
//                                          .atZone(ZoneId.systemDefault())
//                                          .toLocalDateTime()),
//            Comparator.reverseOrder()
//        );
//
//    switch (filterType.toLowerCase()) {
//        case "entry":
//            filteredDevices = devices.stream()
//                    .filter(d -> d.getDevicePosition() == DevicePosition.ENTRY)
//                    .sorted(byRecentActivityDesc)
//                    .collect(Collectors.toList());
//            break;
//        case "exit":
//            filteredDevices = devices.stream()
//                    .filter(d -> d.getDevicePosition() == DevicePosition.EXIT)
//                    .sorted(byRecentActivityDesc)
//                    .collect(Collectors.toList());
//            break;
//        case "unassigned":
//            filteredDevices = devices.stream()
//                    .filter(d -> d.getEntrance() == null)
//                    .sorted(byRecentActivityDesc)
//                    .collect(Collectors.toList());
//            break;
//        default:
//            filteredDevices = new ArrayList<>(devices);
//            filteredDevices.sort(byRecentActivityDesc);
//            break;
//    }
//}
//
//    public List<Devices> getFilteredDevices() {
//        if (filteredDevices == null) {
//            filterDevices("all"); // Initialize with all devices
//        }
//        return filteredDevices;
//    }
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

    public void setupBreadcrumb() {
        breadcrumbBean.setManageEntrancesDevices();
    }

    public void searchDevices() {
        applyAllFilters();
    }

// Filter by entrance - called when entrance dropdown changes
    public void filterByEntrance() {
        applyAllFilters();
    }

// Filter by position - called when position dropdown changes  
    public void filterByPosition() {
        applyAllFilters();
    }

    // Clear all filters button
    public void clearAllFilters() {
        searchQuery = "";
        selectedEntranceFilter = null;
        selectedPositionFilter = "";
        currentFilter = "all";
        applyAllFilters();
    }

    // Enhanced filter method that combines your existing logic with new filters
    private void applyAllFilters() {
        // Start with all devices
        List<Devices> baseFilteredDevices;

        // Apply your existing filter logic first
        Comparator<Devices> byRecentActivityDesc = Comparator
                .comparing(
                        d -> Optional.ofNullable(d.getUpdatedAt())
                                .orElse(d.getCreatedAt().toInstant()
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime()),
                        Comparator.reverseOrder()
                );

        switch (currentFilter.toLowerCase()) {
            case "entry":
                baseFilteredDevices = devices.stream()
                        .filter(d -> d.getDevicePosition() == DevicePosition.ENTRY)
                        .collect(Collectors.toList());
                break;
            case "exit":
                baseFilteredDevices = devices.stream()
                        .filter(d -> d.getDevicePosition() == DevicePosition.EXIT)
                        .collect(Collectors.toList());
                break;
            case "unassigned":
                baseFilteredDevices = devices.stream()
                        .filter(d -> d.getEntrance() == null)
                        .collect(Collectors.toList());
                break;
            default:
                baseFilteredDevices = new ArrayList<>(devices);
                break;
        }

        // Now apply additional filters on top of the base filter
        filteredDevices = baseFilteredDevices.stream()
                .filter(this::matchesSearchQuery)
                .filter(this::matchesEntranceFilter)
                .filter(this::matchesPositionFilter)
                .sorted(byRecentActivityDesc)
                .collect(Collectors.toList());
    }

    // Filter predicates - these check individual conditions
    private boolean matchesSearchQuery(Devices device) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }

        String query = searchQuery.toLowerCase();
        return (device.getDeviceName() != null && device.getDeviceName().toLowerCase().contains(query))
                || (device.getDeviceId() != null && device.getDeviceId().toLowerCase().contains(query));
    }

    private boolean matchesEntranceFilter(Devices device) {
        if (selectedEntranceFilter == null) {
            return true;
        }

        return device.getEntrance() != null
                && device.getEntrance().getId()==(selectedEntranceFilter.getId());
    }

    private boolean matchesPositionFilter(Devices device) {
        if (selectedPositionFilter == null || selectedPositionFilter.isEmpty()) {
            return true;
        }

        if ("UNASSIGNED".equals(selectedPositionFilter)) {
            return device.getEntrance() == null;
        }

        return selectedPositionFilter.equals(device.getDevicePosition().name());
    }

// Get active filters text for display
    public String getActiveFiltersText() {
        List<String> activeFilters = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            activeFilters.add("Search: " + searchQuery);
        }

        if (selectedEntranceFilter != null) {
            activeFilters.add("Entrance: " + selectedEntranceFilter.getEntranceName());
        }

        if (selectedPositionFilter != null && !selectedPositionFilter.isEmpty()) {
            String positionLabel = "UNASSIGNED".equals(selectedPositionFilter)
                    ? "Unassigned" : selectedPositionFilter.toLowerCase();
            activeFilters.add("Position: " + positionLabel);
        }

        return activeFilters.isEmpty() ? "" : String.join(", ", activeFilters);
    }

// Update your existing filterDevices method to work with new system
    public void filterDevices(String filterType) {
        this.currentFilter = filterType;
        // Clear other filters when using stats-based filtering
        searchQuery = "";
        selectedEntranceFilter = null;
        selectedPositionFilter = "";
        applyAllFilters();
    }

// Update your existing methods to refresh filters after operations
    public void saveDevice() {
        try {
            if (!validateDeviceData()) {
                String details = "Device data validation failed.";
                auditLogService.logActivity(isNewDevice() ? AuditActionType.CREATE : AuditActionType.UPDATE, "Manage Devices", ActionResult.FAILED, details, userSession.getCurrentUser());
                return;
            }

            assignSelectedEntrance();
            device.setDeleted(false);

            if (isNewDevice()) {
                deviceService.registerDevice(device);
                devices.add(device);
                String successDetail = "Successfully added new " + device.getDevicePosition() + " device with ID: " + device.getId() + " for " + device.getEntrance().getEntranceName();
                auditLogService.logActivity(AuditActionType.CREATE, "Manage Devices", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());
                JSF.addSuccessMessage("Device added successfully.");
            } else {
                deviceService.updateDevice(device);
                String successDetail = "Successfully updated " + device.getDevicePosition() + " device with ID: " + device.getId() + " for " + device.getEntrance().getEntranceName();
                auditLogService.logActivity(AuditActionType.UPDATE, "Save Device", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());
                updateDeviceInList(device);
                JSF.addSuccessMessage("Device updated successfully.");
            }

            reset();
            // Apply current filters after saving
            applyAllFilters();

        } catch (Exception e) {
            String errorDetail = "Failed to save device with ID: " + (device.getId() != 0 ? device.getId() : "Unknown") + ". Error: " + e.getMessage();
            auditLogService.logActivity(isNewDevice() ? AuditActionType.CREATE : AuditActionType.UPDATE, "Save Device", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
            JSF.addErrorMessage("Error saving device: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void deleteDevice(Devices selected) {
        try {
            if (selected == null || selected.getId() == 0) {
                String details = "No device selected for deletion.";
                auditLogService.logActivity(AuditActionType.DELETE, "Delete Device", ActionResult.FAILED, details, userSession.getCurrentUser());
                JSF.addErrorMessage("No device selected for deletion.");
                return;
            }

            deviceService.deleteDevice(selected.getId());

            String successDetail = "Successfully deleted " + selected.getDevicePosition() + " device with ID: " + selected.getDeviceId() + " for " + selected.getEntrance().getEntranceName();
            auditLogService.logActivity(AuditActionType.DELETE, "Delete Device", ActionResult.SUCCESS, successDetail, userSession.getCurrentUser());

            // Refresh devices list and apply current filters
            devices = deviceService.getAllDevices();
            applyAllFilters();

            JSF.addWarningMessage("Device is deleted.");
        } catch (Exception e) {
            String errorDetail = "Failed to delete device with ID: " + (selected != null ? selected.getDeviceId() : "Unknown") + ". Error: " + e.getMessage();
            auditLogService.logActivity(AuditActionType.DELETE, "Delete Device", ActionResult.FAILED, errorDetail, userSession.getCurrentUser());
            JSF.addErrorMessage("Error deleting device: " + e.getLocalizedMessage());
        }
    }
}
