/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * Entity class for Biometric Data
 * Represents the biometric data of employees.
 * 
 * @author PhilipManteAsare
 */
@Entity
@Table(name = "BiometricData")
public class BiometricData extends EntityModel implements Serializable {


    @OneToOne
    @JoinColumn(name = "ghana_card_number", nullable = false, referencedColumnName = "GHANA_CARD_NUMBER") 
    private Employee employee;

    @Lob
    @Column(name = "fingerprint_data", nullable = false, columnDefinition = "CLOB")
    private String fingerprintData;

//    @Lob
//    @Column(name = "facial_recognition_data", nullable = false)
//    private String facialRecognitionData;

//    @Column(name = "created_at", nullable = false)
//    private LocalDateTime createdAt;

    // Getters and Setters
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getFingerprintData() {
        return fingerprintData;
    }

    public void setFingerprintData(String fingerprintData) {
        this.fingerprintData = fingerprintData;
    }
//
//    public String getFacialRecognitionData() {
//        return facialRecognitionData;
//    }
//
//    public void setFacialRecognitionData(String facialRecognitionData) {
//        this.facialRecognitionData = facialRecognitionData;
//    }




    public BiometricData(Long biometricDataId, Employee employee, String fingerprintData, String facialRecognitionData, LocalDateTime createdAt) {
        this.employee = employee;
        this.fingerprintData = fingerprintData;
//        this.facialRecognitionData = facialRecognitionData;
    }


    // Constructors
    public BiometricData() {
    }
}
