/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import com.margins.STIM.entity.enums.LocationState;
import com.margins.STIM.util.DateFormatter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "EMPLOYEE_ENTRANCE_STATE",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"employee_id", "entrance_device_id"},
                name = "uk_employee_entrance"
        )
)
@Getter
@Setter
public class EmployeeEntranceState implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entrance_device_id", referencedColumnName = "id", nullable = false)
    private Entrances entrance;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_used_id")
    private Devices deviceUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_state", nullable = false)
    private LocationState currentState;

    // Audit fields
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy; // Username of who last updated this state

    @Column(name = "reset_reason", length = 500)
    private String resetReason; // Why was this state reset? (admin override, mode change, etc.)

    @Column(name = "is_deleted")
    private boolean deleted = false;
    
    public String getFormattedLastModifiedDate() {
        return lastModifiedDate != null
                ? DateFormatter.forDateTimes(lastModifiedDate)
                : "N/A";
    }
    
    public EmployeeEntranceState() {
    }

    public EmployeeEntranceState(Employee employee, Entrances entrance, LocationState currentState, Devices device) {
        this.employee = employee;
        this.entrance = entrance;
        this.currentState = currentState;
        this.deviceUsed = device;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        lastModifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }

    // Helper method for state updates
    public void updateState(LocationState newState, String updatedBy, String reason) {
        this.currentState = newState;
        this.lastUpdatedBy = updatedBy;
        this.resetReason = reason;
        this.lastModifiedDate = LocalDateTime.now();
    }
}
