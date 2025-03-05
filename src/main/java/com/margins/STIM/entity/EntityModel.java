/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.time.LocalDate;
import java.util.Date;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 *
 * @author PhilipManteAsare
 */
@MappedSuperclass
public class EntityModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    private LocalDate updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void setCreatedAt() {
        Date currentdate = new Date();
        this.createdAt = currentdate;
    }
    
    public LocalDate getUpdatedAt() {
        return updatedAt;
    }
    
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDate.now();
    }
    
    
}

