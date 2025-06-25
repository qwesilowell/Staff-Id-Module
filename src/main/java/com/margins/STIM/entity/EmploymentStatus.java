/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table (name ="EMPLOYMENT_STATUS")
@Cacheable(false)
public class EmploymentStatus extends EntityModel implements Serializable {
    
    
    @Column(name ="STATUS", nullable= false)
    private String empstatus;

    @Override
    public String toString() {
        return empstatus;
    }
    
    }
