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

@Named("deviceBean")
@ViewScoped
public class DeviceMangementBean implements Serializable {

    private Devices device;

    private List<Devices> devices = new ArrayList<>();
    private List<Devices> filteredDevices = new ArrayList<>();
    private List<Entrances> availableEntrances = new ArrayList<>();

    private String currentFilter = "all";         
    private String searchQuery = "";
    private Entrances selectedEntranceFilter;      
    private String selectedPositionFilter = "";    // "", ENTRY, EXIT, UNASSIGNED

    private String deviceId;
    private String deviceName;
    private DevicePosition devicePosition;         
    private Entrances selectedEntrance;            

    private boolean editMode = false;

 
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
        clearTempVariables();
        applyAllFilters();
    }

    
    public void saveDevice() {
        try {
            assignTempVariablesToDevice();

            if (!validateDeviceData()) {
                auditLogService.logActivity(isNewDevice() ? AuditActionType.CREATE : AuditActionType.UPDATE,
                        "Manage Devices", ActionResult.FAILED, "Validation failed", userSession.getCurrentUser());
                return;
            }

            device.setDeleted(false);

            if (isNewDevice()) {
                deviceService.registerDevice(device);
                devices.add(device);

                auditLogService.logActivity(AuditActionType.CREATE, "Manage Devices",
                        ActionResult.SUCCESS,
                        "Added " + device.getDevicePosition() + " deviceId=" + device.getDeviceId(),
                        userSession.getCurrentUser());

                JSF.addSuccessMessage("Device added successfully.");
            } else {
                deviceService.updateDevice(device);
                replaceDeviceInList(device);

                auditLogService.logActivity(AuditActionType.UPDATE, "Manage Devices",
                        ActionResult.SUCCESS,
                        "Updated " + device.getDevicePosition() + " deviceId=" + device.getDeviceId(),
                        userSession.getCurrentUser());

                JSF.addSuccessMessage("Device updated successfully.");
            }

            reset();
            applyAllFilters();

        } catch (Exception e) {
            auditLogService.logActivity(isNewDevice() ? AuditActionType.CREATE : AuditActionType.UPDATE,
                    "Manage Devices", ActionResult.FAILED,
                    "Save error: " + e.getMessage(), userSession.getCurrentUser());
            JSF.addErrorMessage("Failed Saving device ");
            e.printStackTrace();
        }
    }

    public void editDevice(Devices selected) {
        if (selected == null) {
            JSF.addWarningMessage("No device selected to edit.");
            return;
        }
        JSF.addSuccessMessageWithSummary("Edit Mode", "Editing " + selected.getDeviceId());
        this.device = selected;
        this.editMode = true;
        assignDeviceToTempVariables(); 
    }

    public void deleteDevice(Devices selected) {
        try {
            if (selected == null || selected.getId() == 0) {
                auditLogService.logActivity(AuditActionType.DELETE, "Manage Devices",
                        ActionResult.FAILED, "No device selected", userSession.getCurrentUser());
                JSF.addErrorMessage("No device selected for deletion.");
                return;
            }

            deviceService.deleteDevice(selected.getId());
            auditLogService.logActivity(AuditActionType.DELETE, "Manage Devices",
                    ActionResult.SUCCESS,
                    "Deleted deviceId=" + selected.getDeviceId(),
                    userSession.getCurrentUser());

            // refresh list & filters
            devices = deviceService.getAllDevices();
            applyAllFilters();

            JSF.addWarningMessage("Device is deleted.");
        } catch (Exception e) {
            auditLogService.logActivity(AuditActionType.DELETE, "Manage Devices",
                    ActionResult.FAILED, "Delete error: " + e.getMessage(), userSession.getCurrentUser());
            JSF.addErrorMessage("Error deleting device: " + e.getLocalizedMessage());
        }
    }

    public void cancelEdit() {
        reset();
        JSF.addWarningMessage("Edit cancelled.");
    }

    public void reset() {
        device = new Devices();
        editMode = false;
        JSF.addWarningMessage("Reset Succesful");
        clearTempVariables();
    }

    private boolean isNewDevice() {
        // You use editMode to decide new vs edit
        return !editMode || device == null || device.getId() == 0;
    }

    private void replaceDeviceInList(Devices updated) {
        for (int i = 0; i < devices.size(); i++) {
            if (devices.get(i).getId() == updated.getId()) {
                devices.set(i, updated);
                return;
            }
        }
    }

    // =========================================================
    // Form temp fields <-> entity sync (because XHTML binds to bean temp fields)
    // =========================================================
    private void assignTempVariablesToDevice() {
        if (device == null) {
            device = new Devices();
        }
        device.setDeviceId(deviceId != null ? deviceId.trim() : null);
        device.setDeviceName(deviceName != null ? deviceName.trim() : null);
        device.setDevicePosition(devicePosition);
        device.setEntrance(selectedEntrance); // can be null (unassigned)
    }

    private void assignDeviceToTempVariables() {
        if (device == null) {
            System.out.println("Editing Device is Null");
            return;
        }
        deviceId = device.getDeviceId();
        deviceName = device.getDeviceName();
        devicePosition = device.getDevicePosition();
        selectedEntrance = device.getEntrance();
    }

    private void clearTempVariables() {
        deviceId = "";
        deviceName = "";
        devicePosition = null;
        selectedEntrance = null;
    }

    // =========================================================
    // Validation used by saveDevice()
    // =========================================================
    private boolean validateDeviceData() {
        boolean ok = true;

        if (deviceId == null || deviceId.trim().isEmpty()) {
            JSF.addWarningMessage("Please enter a Device ID");
            ok = false;
        }
        if (deviceName == null || deviceName.trim().isEmpty()) {
            JSF.addWarningMessage("Please enter a Device Name");
            ok = false;
        }
        if (devicePosition == null) {
            JSF.addWarningMessage("Please select a Device Position (Entry or Exit)");
            ok = false;
        }
        // Entrance can be optional; if required, uncomment:
        // if (selectedEntrance == null) { JSF.addWarningMessage("Please select an Entrance"); ok = false; }

        return ok;
    }

    // =========================================================
    // Position toggle (form & per-card)
    // =========================================================
    // Called by the form Entry/Exit buttons: set temporary position field
    public void setDevicePosition(String position) {
        updateDevicePosition(position);
    }

    // Used by the list card buttons to update a single device and persist immediately
//    public void updateDevicePosition(Devices d, String position) {
//        if (d == null) {
//            return;
//        }
//        if ("ENTRY".equalsIgnoreCase(position)) {
//            d.setDevicePosition(DevicePosition.ENTRY);
//        } else if ("EXIT".equalsIgnoreCase(position)) {
//            d.setDevicePosition(DevicePosition.EXIT);
//        }
//        try {
//            deviceService.updateDevice(d);
//            JSF.addSuccessMessage("Device position updated.");
//            applyAllFilters();
//        } catch (Exception e) {
//            JSF.addErrorMessage("Failed to update device position: " + e.getMessage());
//        }
//    }

    // Helper to set the temp position field from form toggle
    public void updateDevicePosition(String position) {
        if ("ENTRY".equalsIgnoreCase(position)) {
            devicePosition = DevicePosition.ENTRY;
        } else if ("EXIT".equalsIgnoreCase(position)) {
            devicePosition = DevicePosition.EXIT;
        }
    }

    // NOTE: Your XHTML compares result to a string:
    //   #{deviceBean.isTempPositionActive("ENTRY") == 'ENTRY' ? 'active' : ''}
    // so we return the same string when active, or empty otherwise.
    public String isTempPositionActive(String position) {
        if (devicePosition == null || position == null) {
            return "";
        }
        return devicePosition.name().equalsIgnoreCase(position) ? "active" : "";
    }

    // =========================================================
    // Search / Filter logic (wired to your XHTML)
    // =========================================================
    public void setupBreadcrumb() {
        breadcrumbBean.setManageEntrancesDevices();
    }

    public void searchDevices() {
        applyAllFilters();
    }

    public void filterByEntrance() {
        applyAllFilters();
    }

    public void filterByPosition() {
        applyAllFilters();
    }

    public void clearAllFilters() {
        searchQuery = "";
        selectedEntranceFilter = null;
        selectedPositionFilter = "";
        currentFilter = "all";
        JSF.addSuccessMessage("Filters Cleared");
        applyAllFilters();
    }

    public void filterDevices(String filterType) {
        this.currentFilter = (filterType != null ? filterType : "all");
        // reset other filters when using stat tiles
        searchQuery = "";
        selectedEntranceFilter = null;
        selectedPositionFilter = "";
        applyAllFilters();
    }

    private void applyAllFilters() {
        List<Devices> base;

        switch (currentFilter.toLowerCase()) {
            case "entry":
                base = devices.stream()
                        .filter(d -> d.getDevicePosition() == DevicePosition.ENTRY)
                        .collect(Collectors.toList());
                break;
            case "exit":
                base = devices.stream()
                        .filter(d -> d.getDevicePosition() == DevicePosition.EXIT)
                        .collect(Collectors.toList());
                break;
            case "unassigned":
                base = devices.stream()
                        .filter(d -> d.getEntrance() == null)
                        .collect(Collectors.toList());
                break;
            default:
                base = new ArrayList<>(devices);
        }

        Comparator<Devices> byRecentActivityDesc = Comparator.comparing(
                d -> Optional.ofNullable(d.getUpdatedAt())
                        .orElse(d.getCreatedAt().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()),
                Comparator.reverseOrder());

        filteredDevices = base.stream()
                .filter(this::matchesSearchQuery)
                .filter(this::matchesEntranceFilter)
                .filter(this::matchesPositionFilter)
                .sorted(byRecentActivityDesc)
                .collect(Collectors.toList());
    }

    private boolean matchesSearchQuery(Devices d) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return true;
        }
        String q = searchQuery.toLowerCase();
        return (d.getDeviceName() != null && d.getDeviceName().toLowerCase().contains(q))
                || (d.getDeviceId() != null && d.getDeviceId().toLowerCase().contains(q));
    }

    private boolean matchesEntranceFilter(Devices d) {
        if (selectedEntranceFilter == null) {
            return true;
        }
        return d.getEntrance() != null && d.getEntrance().getId() == selectedEntranceFilter.getId();
    }

    private boolean matchesPositionFilter(Devices d) {
        if (selectedPositionFilter == null || selectedPositionFilter.isEmpty()) {
            return true;
        }
        if ("UNASSIGNED".equalsIgnoreCase(selectedPositionFilter)) {
            return d.getEntrance() == null;
        }
        return d.getDevicePosition() != null
                && selectedPositionFilter.equalsIgnoreCase(d.getDevicePosition().name());
    }

    public String getActiveFiltersText() {
        List<String> parts = new ArrayList<>();
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            parts.add("Search: " + searchQuery);
        }
        if (selectedEntranceFilter != null) {
            parts.add("Entrance: " + selectedEntranceFilter.getEntranceName());
        }
        if (selectedPositionFilter != null && !selectedPositionFilter.isEmpty()) {
            parts.add("Position: " + ("UNASSIGNED".equals(selectedPositionFilter) ? "Unassigned" : selectedPositionFilter));
        }
        return parts.isEmpty() ? "" : String.join(", ", parts);
    }

    public List<Entrances> searchEntrances(String query) {
        if (availableEntrances == null || availableEntrances.isEmpty()) {
            availableEntrances = entranceService.findAllEntrances();
        }
        if (query == null || query.trim().isEmpty()) {
            return availableEntrances;
        }
        final String q = query.toLowerCase();
        return availableEntrances.stream()
                .filter(e -> (e.getEntranceName() != null && e.getEntranceName().toLowerCase().contains(q))
                || (e.getEntranceDeviceId() != null && e.getEntranceDeviceId().toLowerCase().contains(q)))
                .collect(Collectors.toList());
    }


    public int getTotalDevices() {
        return devices != null ? devices.size() : 0;
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

    public boolean filterActive(String key) {
        return currentFilter != null && currentFilter.equalsIgnoreCase(key);
    }

    public Devices getDevice() {
        return device;
    }

    public void setDevice(Devices device) {
        this.device = device;
    }

    public List<Devices> getDevices() {
        return devices;
    }

    public void setDevices(List<Devices> devices) {
        this.devices = devices;
    }

    public List<Devices> getFilteredDevices() {
        return filteredDevices;
    }

    public void setFilteredDevices(List<Devices> filteredDevices) {
        this.filteredDevices = filteredDevices;
    }

    public List<Entrances> getAvailableEntrances() {
        return availableEntrances;
    }

    public void setAvailableEntrances(List<Entrances> availableEntrances) {
        this.availableEntrances = availableEntrances;
    }

    public String getCurrentFilter() {
        return currentFilter;
    }

    public void setCurrentFilter(String currentFilter) {
        this.currentFilter = currentFilter;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public Entrances getSelectedEntranceFilter() {
        return selectedEntranceFilter;
    }

    public void setSelectedEntranceFilter(Entrances selectedEntranceFilter) {
        this.selectedEntranceFilter = selectedEntranceFilter;
    }

    public String getSelectedPositionFilter() {
        return selectedPositionFilter;
    }

    public void setSelectedPositionFilter(String selectedPositionFilter) {
        this.selectedPositionFilter = selectedPositionFilter;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public DevicePosition getDevicePosition() {
        return devicePosition;
    }

    public void setDevicePosition(DevicePosition devicePosition) {
        this.devicePosition = devicePosition;
    }

    public Entrances getSelectedEntrance() {
        return selectedEntrance;
    }

    public void setSelectedEntrance(Entrances selectedEntrance) {
        this.selectedEntrance = selectedEntrance;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }
}
