/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.enums.EntranceMode;
import com.margins.STIM.service.EntrancesService;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("entranceBean")
@ViewScoped
@Getter
@Setter
public class EntranceBean implements Serializable {

    @Inject
    private EntrancesService entranceService;

    @Inject
    private BreadcrumbBean breadcrumbBean;

    private List<Entrances> entrances;
    private Entrances selectedEntrance;
    private String searchQuery;
    private List<EntranceMode> entranceModes;

    @PostConstruct
    public void init() {
        loadEntrances();
        selectedEntrance = new Entrances(); // Initialize to prevent null issues
        loadEntrances();
        entranceModes = Arrays.asList(EntranceMode.values());
    }

    public void loadEntrances() {
        entrances = entranceService.findAllEntrances();
    }

    public void setupBreadcrumb() {
        breadcrumbBean.setManageEntrancesBreadcrumb();
    }

    public void searchEntrances() {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            entrances = entranceService.searchEntrances(searchQuery);
        } else {
            loadEntrances();
        }
    }

    public void prepareNewEntrance() {
        System.out.println("Preparing to add new entrance"); // Debug log
        selectedEntrance = new Entrances(); // Ensure a fresh instance
    }

    public void prepareEditEntrance(Entrances entrance) {
        System.out.println("Preparing to edit entrance with ID: " + entrance.getEntranceDeviceId()); // Debug log
        selectedEntrance = entrance; // Set the selected entrance to the one being edited
    }

    public void addEntrance() {
        try {
            // Make sure the entrance ID is set (since you're manually entering it)
            if (selectedEntrance.getEntranceDeviceId() == null || selectedEntrance.getEntranceDeviceId().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Entrance ID is required."));
                return; // Prevent saving if ID is missing
            }

            System.out.println("Adding new entrance with ID: " + selectedEntrance.getEntranceDeviceId());
            entranceService.addEntrance(selectedEntrance); // Save the new entrance
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrance created successfully!"));
            loadEntrances(); // Refresh the list of entrances
            selectedEntrance = new Entrances(); // Reset the form after adding
        } catch (Exception e) {
            System.out.println("Error adding entrance: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to add entrance."));
            e.printStackTrace();
        }
    }

    public void updateEntrance() {
        try {
            // Ensure the entrance ID is valid before updating
            if (selectedEntrance.getEntranceDeviceId() == null || selectedEntrance.getEntranceDeviceId().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Entrance ID is required."));
                return; // Prevent updating if ID is missing
            }

            System.out.println("Updating entrance with ID: " + selectedEntrance.getEntranceDeviceId());
            entranceService.updateEntrance(selectedEntrance.getId(), selectedEntrance); // Update the entrance
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrance updated successfully!"));
            loadEntrances(); // Refresh the list
            selectedEntrance = new Entrances(); // Reset form after updating
        } catch (Exception e) {
            System.out.println("Error updating entrance: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update entrance."));
            e.printStackTrace();
        }
    }

    public void updateEntranceMode(Entrances entrance) {
        try {
            System.out.println("Updating mode for entrance ID: " + entrance.getEntranceName());
            entranceService.updateEntrance(entrance.getId(), entrance);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrance mode updated successfully!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update mode."));
            e.printStackTrace();
        }
    }

    public void saveOrUpdateEntrance() {
        System.out.println("saveOrUpdateEntrance() called");
        addEntrance();
    }

    public void confirmDelete(Entrances entrance) {
        selectedEntrance = entrance;
    }

    public void deleteEntrance() {
        try {
            entranceService.deleteEntrance(selectedEntrance.getId());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Entrance deleted successfully!"));
            loadEntrances();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete entrance."));
        }
    }
}
