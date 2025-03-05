/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */



import com.margins.STIM.entity.Access_Levels;
import com.margins.STIM.service.AccessLevelsService;
import java.io.Serializable;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named("accessLevelsBean")
@ViewScoped
public class CreateAccessLevelsBean implements Serializable {

    @EJB
    private AccessLevelsService accessLevelsService;

    private List<Access_Levels> accessLevels;
    private Access_Levels selectedAccessLevel;

    @PostConstruct
    public void init() {
        loadAccessLevels();
        if (accessLevels != null && !accessLevels.isEmpty()) {
            selectedAccessLevel = accessLevels.get(0); // Pick the first access level
        } else {
            selectedAccessLevel = new Access_Levels(); // Prevent null issues
        }
    }


    public void loadAccessLevels() {
        accessLevels = accessLevelsService.findAllAccessLevels();
        System.out.println("Access levels loaded: " + accessLevels.size()); // Debug log
    }



    public void prepareNewAccessLevel() {
        selectedAccessLevel = new Access_Levels(); // Reset for a new entry
    }

    public void prepareEditAccessLevel(Access_Levels accessLevel) {
        selectedAccessLevel = accessLevel;  // Set the access level for editing
        System.out.println("Selected Access Level: " + selectedAccessLevel.getLevel_name()); // Debug log
    }


    public void saveAccessLevel() {
        try {
            System.out.println("Saving Access Level: " + selectedAccessLevel.getLevel_name()); // Debug log
            System.out.println("Is Allowed: " + selectedAccessLevel.getIsAllowed()); // Debug log

            accessLevelsService.createAccessLevel(selectedAccessLevel); // Save new access level

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Access Level added successfully!"));

            loadAccessLevels(); // Refresh the access levels list
            selectedAccessLevel = new Access_Levels(); // Reset form
        } catch (Exception e) {
            System.out.println("Error saving access level: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to save access level."));
            e.printStackTrace();
        }
    }


    public void updateAccessLevel() {
        try {
            accessLevelsService.updateAccessLevel(selectedAccessLevel.getId(), selectedAccessLevel); // Update existing access level
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Access Level updated successfully!"));
            loadAccessLevels(); // Refresh the access levels list
            selectedAccessLevel = new Access_Levels(); // Reset form
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update access level."));
            e.printStackTrace();
        }
    }

    public void confirmDelete(Access_Levels accessLevel) {
        selectedAccessLevel = accessLevel; // Set the access level to be deleted
    }

    public void deleteAccessLevel() {
        try {
            accessLevelsService.deleteAccessLevel(selectedAccessLevel.getId()); // Delete the access level
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Access Level deleted successfully!"));
            loadAccessLevels(); // Refresh the list
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to delete access level."));
            e.printStackTrace();
        }
    }

    // Getters and Setters
    public List<Access_Levels> getAccessLevels() {
        return accessLevels;
    }

    public Access_Levels getSelectedAccessLevel() {
        return selectedAccessLevel;
    }

    public void setSelectedAccessLevel(Access_Levels selectedAccessLevel) {
        this.selectedAccessLevel = selectedAccessLevel;
    }
}
