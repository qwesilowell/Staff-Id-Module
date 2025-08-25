/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Bean;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.AnomalyNotification;
import com.margins.STIM.entity.model.Notification;
import com.margins.STIM.service.NotificationService;
import com.margins.STIM.util.DateFormatter;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import org.primefaces.PrimeFaces;

@Named("notificationBean")
@SessionScoped
public class NotificationBean implements Serializable {

    @Inject
    private NotificationService notificationService;

    @Inject
    private UserSession userSession;

    private List<Notification> notifications;
    private Long unreadCount;
    private int offset = 0;

    private int currentUserId;

    @PostConstruct
    public void init() {
        currentUserId = userSession.getCurrentUser().getId();
    }

    public void loadNotifications() {
        notifications = notificationService.getUnreadNotificationsForUser(currentUserId, offset);
        unreadCount = notificationService.getUnreadCountForUser(currentUserId);
    }

    public String showTime(Notification n) {
        return n.getCreatedAt() != null ? DateFormatter.formatDateAsTimeString(n.getCreatedAt()) : "N/A";
    }

    public void refresh() {
        loadNotifications();
    }

    public void markAllAsRead() {
        notificationService.markAllAsRead(currentUserId);
        loadNotifications();
    }

    public void clearAll() {
        // Could iterate and dismiss, or hard delete if you add a method
        notifications.forEach(n -> notificationService.dismissNotification(n.getId(), currentUserId));
        loadNotifications();
    }

    public void markAsRead(int notificationId) {
        notificationService.markAsRead(notificationId, currentUserId);
        loadNotifications();
    }

    public void dismiss(Notification n) {
        notificationService.dismissNotification(n.getId(), currentUserId);
        loadNotifications();
    }

    public String openNotification(Notification notification) {
        markAsRead(notification.getId());
        if (notification instanceof AnomalyNotification anomalyNotif) {
            if (anomalyNotif.getAnomaly() != null) {
                FacesContext.getCurrentInstance().getExternalContext().getFlash().put("anomalyRef", anomalyNotif.getAnomaly().getAnomalyRef());
                FacesContext.getCurrentInstance().getExternalContext().getFlash().put("anomalyStatus", anomalyNotif.getAnomaly().getAnomalyStatus());

                FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", "Displaying Anomaly " + anomalyNotif.getAnomaly().getAnomalyRef()));
                PrimeFaces.current().ajax().update("navBar");
                //Its best to use full context 
                return "/app/Anomalies/anomaliesPage?faces-redirect=true";
            }
            return "/app/Anomalies/anomaliesPage?faces-redirect=true";
        }

        //Idea:redirect to other pages showing notification for now redirection to anomaly Page
        return "/app/Anomalies/anomaliesPage?faces-redirect=true";
    }

    public String getIconClass(Notification n) {
        return n.getIconClass() != null ? n.getIconClass() : "fa fa-bell";
    }

    // Getters
    public List<Notification> getNotifications() {
        return notifications;
    }

    public Long getUnreadCount() {
        return notificationService.getUnreadCountForUser(currentUserId);
    }

    public void handleNotificationClick() {
        String idParam = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("notificationId");

        if (idParam != null) {
            int notifId = Integer.parseInt(idParam);
            markAsRead(notifId);
        }
    }
}
