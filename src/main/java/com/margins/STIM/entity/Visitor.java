/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Entity
@Getter
@Setter
@Table(name = "VISITOR")
public class Visitor extends EntityModel implements Serializable {

    @Column(name = "FORENAME", nullable = false)
    private String forenames;

    @Column(name = "SURNAME", nullable = false)
    private String surname;

    @Column(name = "NATIONAL_ID", nullable = false)
    private String nationalId;

    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "GENDER", nullable = false)
    private String gender;

    @Column(name = "PHONE_NUMBER", nullable = false)
    private String phoneNumber;

    @Column(name = "EMAIL", nullable = false)
    private String email;

    @OneToMany(mappedBy = "visitor")
    private List<VisitorAccess> accessList = new ArrayList<>();
    
}
