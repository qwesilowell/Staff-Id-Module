/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "view_permissions")
@Getter
@Setter
public class ViewPermission extends EntityModel implements Serializable {

    private String pagePath; // e.g. "/admin/manageUsers.xhtml"
    
    private String displayName; // e.g. "Manage Users"

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ViewPermission other = (ViewPermission) obj;
        return this.getId() == other.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
    
}
