/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.util;

import com.margins.STIM.entity.model.Notification;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
public class NotificationEvent {

    private Notification notification;
    private String action = "CREATED"; // CREATED, READ, MARK_ALL_READ
    private int userId; // for bulk operations

    public NotificationEvent(Notification notification) {
        this.notification = notification;
    }

    public NotificationEvent(Notification notification, String action) {
        this.notification = notification;
        this.action = action;
    }

    public NotificationEvent(int userId, String action) {
        this.userId = userId;
        this.action = action;
    }

}
