/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity.model;

import com.margins.STIM.entity.EntityModel;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.NotificationPriority;
import com.margins.STIM.entity.enums.NotificationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
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
@Table(name = "NOTIFICATIONS")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Notification extends EntityModel implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Users recipient;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // For navigation - where should clicking this notification take you?
    @Column(name = "action_url")
    private String actionUrl;

    @Column(name = "icon_class") // For different icons per notification type
    private String iconClass;

    // Optional: expires after certain time
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}


//public enum AnomalyNotificationAction {
//    ASSIGNED, REASSIGNED, ESCALATED, STATUS_UPDATED, RESOLVED
//}
//
//public enum SystemNotificationAction {
//    MAINTENANCE_SCHEDULED, SYSTEM_DOWN, SYSTEM_RESTORED, UPDATE_AVAILABLE
//}
//
//public enum UserNotificationAction {
//    PROFILE_UPDATED, PASSWORD_CHANGED, PERMISSION_GRANTED, ACCOUNT_LOCKED
//}