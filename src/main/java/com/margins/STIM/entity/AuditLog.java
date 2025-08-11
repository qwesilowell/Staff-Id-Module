/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

import com.margins.STIM.entity.enums.ActionResult;
import com.margins.STIM.entity.enums.AuditActionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "audit_logs")
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_performed")
    private AuditActionType action;

    @Column(name = "page_performed")
    private String entityName;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private ActionResult result;

    @Column(name = "action_details")
    private String details;

    @ManyToOne
    @JoinColumn(name = "performed_by_id") // Correct way to map the relationship
    private Users performedBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Override
    public String toString() {
        return "AuditLog [id=" + id + ", action=" + action + "]";
    }
}
