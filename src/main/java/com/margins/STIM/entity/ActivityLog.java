/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Entity
@Table(name = "activity_log")
public class ActivityLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId; // Admin ID or employee Ghana Card

    @Column(name = "action")
    private String action; // e.g., "create_employee", "assign_role"

    @Column(name = "target_id")
    private String targetId; // e.g., role ID, entrance ID

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "result")
    private String result; // "success" or "fail"

    @Column(name = "details")
    private String details;
    
    public ActivityLog() {
    }

    public ActivityLog(String userId, String action, String targetId, String result, String details) {
        this.userId = userId;
        this.action = action;
        this.targetId = targetId;
        this.timestamp = LocalDateTime.now();
        this.result = result;
        this.details = details;
    }
}
