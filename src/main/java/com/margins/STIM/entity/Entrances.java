/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import com.margins.STIM.entity.enums.EntranceMode;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "ENTRANCES")
@Cacheable(false)
@Getter
@Setter
public class Entrances extends EntityModel implements Serializable {

    @Column(name = "ENTRANCE_DEVICE_ID", nullable = false, unique = true)
    private String entranceDeviceId;

    @Column(name = "ENTRANCE_NAME", nullable = false, unique = true)
    private String entranceName;

    @Column(name = "ENTRANCE_LOCATION", nullable = false)
    private String entranceLocation;

    @ManyToMany(mappedBy = "accessibleEntrances")
    private Set<EmployeeRole> allowedRoles = new HashSet<>();

    @ManyToMany(mappedBy = "customEntrances")
    private List<Employee> employees = new ArrayList<>();
    
    // NEW: One entrance can have many devices (entry/exit)
    @OneToMany(mappedBy = "entrance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Devices> devices = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column (name = "ENTRANCE_MODE")
    private EntranceMode entranceMode;

    @PrePersist
    public void prePersist() {
        if (entranceMode == null) {
            entranceMode = EntranceMode.STRICT;
        }
    }
    public Entrances(String entranceDeviceId, String entranceName, String entranceLocation) {
        this.entranceDeviceId = entranceDeviceId;
        this.entranceName = entranceName;
        this.entranceLocation = entranceLocation;
    }

    public Entrances() {
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.entranceDeviceId);
        hash = 97 * hash + Objects.hashCode(this.entranceName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Entrances other = (Entrances) obj;
        if (!Objects.equals(this.entranceDeviceId, other.entranceDeviceId)) {
            return false;
        }
        return Objects.equals(this.entranceName, other.entranceName);
    }
    
    @Override
    public String toString() {
        return "Entrances{"
                + "entranceDeviceId='" + entranceDeviceId + '\''
                + ", entranceName='" + entranceName + '\''
                + '}';
    }
    
}
