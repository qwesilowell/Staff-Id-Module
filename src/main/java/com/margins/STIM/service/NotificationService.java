/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.entity.AccessAnomaly;
import com.margins.STIM.entity.AnomalyNotification;
import com.margins.STIM.entity.Notify;
import com.margins.STIM.entity.UserNotification;
import com.margins.STIM.entity.Users;
import com.margins.STIM.entity.enums.AnomalySeverity;
import com.margins.STIM.entity.enums.AnomalyStatus;
import com.margins.STIM.entity.enums.NotificationPriority;
import com.margins.STIM.entity.enums.NotificationStatus;
import com.margins.STIM.entity.enums.UserType;
import com.margins.STIM.entity.model.Notification;
import com.margins.STIM.util.AnomalyMessageBuilder;
import com.margins.STIM.util.NotificationEvent;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.enterprise.event.Event;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@TransactionManagement(TransactionManagementType.CONTAINER)
public class NotificationService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Event<NotificationEvent> notificationEvent; // For real-time updates

    @Inject
    private User_Service userService;

    // ===== CREATION METHODS =====
    /**
     * Create anomaly notification when anomaly is assigned/updated
     *
     * @param anomaly
     * @param recipient
     * @param action
     */
    public void createAnomalyNotification(AccessAnomaly anomaly, Users recipient) {
        try {
            AnomalyNotification notification = new AnomalyNotification();
            notification.setAnomaly(anomaly);
            notification.setRecipient(recipient);
            // Title adapts if resolved

            String title = AnomalyMessageBuilder.buildTitle(anomaly);
            if (anomaly.getAnomalyStatus() == AnomalyStatus.RESOLVED) {
                title = "[RESOLVED] " + title;
            }
            notification.setTitle(title);

            // Always keep the original anomaly description
            String description = AnomalyMessageBuilder.buildDescription(anomaly);
            if (anomaly.getAnomalyStatus() == AnomalyStatus.RESOLVED) {
                description += " (This anomaly has been marked as resolved.)";
            }
            notification.setMessage(description);

            notification.setActionUrl("/app/Anomalies/anomaliesPage.xhtml?id=" + anomaly.getId());
            notification.setIconClass(getAnomalyIcon(anomaly.getAnomalySeverity(), anomaly.getAnomalyStatus()));
            notification.setPriority(mapSeverityToPriority(anomaly.getAnomalySeverity()));

            // Set expiration (optional - e.g., 7 days for resolved anomalies)
            if (anomaly.getAnomalyStatus() == AnomalyStatus.RESOLVED) {
                notification.setExpiresAt(LocalDateTime.now().plusDays(1));
            }

            saveNotification(notification);

            // Fire event for real-time updates
            notificationEvent.fire(new NotificationEvent(notification));

            System.out.println("Created anomaly notification for user: "
                    + recipient.getId() + " anomaly: " + anomaly.getId());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to create notification " + e.getMessage());
        }
    }

    //Notify all admins of an anOMALY
    public void notifyAllAdminsofAnomaly(AccessAnomaly anomaly) {
        try {
            // 1. Fetch all admins
            List<Users> admins = userService.findActiveUsersByType(UserType.ADMIN);

            // 2. Loop through them
            if (admins != null && !admins.isEmpty()) {
                for (Users admin : admins) {
                    createAnomalyNotification(anomaly, admin);
                }
            }else {
                System.out.println("No >>>>>>>>active admins found.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to notify all admins: " + e.getMessage());
        }
    }

    public void navigateToAnomalyPage() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ExternalContext externalContext = context.getExternalContext();
            externalContext.redirect(externalContext.getRequestContextPath() + "/app/Anomalies/anomaliesPage.xhtml");
        } catch (IOException e) {
            // Handle exception appropriately
            System.err.println("Error navigating to anomaly page: " + e.getMessage());
        }
    }

    /**
     * Create system notification
     *
     * public void createSystemNotification(String title, String message, Users
     * recipient, NotificationPriority priority, String actionUrl) { try {
     * SystemNotification notification = new SystemNotification();
     * notification.setTitle(title); notification.setMessage(message);
     * notification.setRecipient(recipient); notification.setPriority(priority);
     * notification.setActionUrl(actionUrl);
     * notification.setIconClass("fa-system");
     *
     * saveNotification(notification); notificationEvent.fire(new
     * NotificationEvent(notification));
     *
     * } catch (Exception e) { logger.error("Failed to create system
     * notification", e); throw new NotificationServiceException("Failed to
     * create notification", e); } }
     */
    /**
     * Broadcast notification to multiple users
     *
     * @param title
     * @param message
     * @param priority
     * @param recipients
     */
    public void createBroadcastNotification(String title, String message, List<Users> recipients, NotificationPriority priority) {
        recipients.forEach(recipient
                -> createSystemNotification(title, message, recipient, priority, null)
        );
    }

    public void createAdminBroadcastNotification(String title,
            String message,
            NotificationPriority priority) {
        List<Users> admins = userService.findActiveUsersByType(UserType.ADMIN);

        for (Users admin : admins) {
            Notification notification = createSystemNotification(
                    title, message, admin, priority, null
            );
            notificationEvent.fire(new NotificationEvent(notification));
        }
    }

    public void createAdminBroadcastNotificationWithRelatedUser(String title,
            String message,
            NotificationPriority priority, Users user) {
        List<Users> admins = userService.findActiveUsersByType(UserType.ADMIN);

        for (Users admin : admins) {
            Notification notification = createSystemNotification(
                    title, message, admin, priority, user
            );
            notificationEvent.fire(new NotificationEvent(notification));
        }
    }

    public UserNotification createSystemNotification(String title, String message, Users recipient, NotificationPriority priority, Users user) {
        UserNotification notification = new UserNotification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRecipient(recipient);
        notification.setPriority(priority);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setRelatedUser(user); // link anomaly if applicable

        saveNotification(notification);

        return notification;
    }

    // ===== RETRIEVAL METHODS =====
    /**
     * Get unread notifications for user
     */
    public List<Notification> getUnreadNotificationsForUser(int userId) {
        return entityManager.createQuery(
                "SELECT n FROM Notification n "
                + "WHERE n.recipient.id = :userId "
                + "AND n.status = :status "
                + "AND (n.expiresAt IS NULL OR n.expiresAt > :now) "
                + "ORDER BY n.priority DESC, n.createdAt DESC",
                Notification.class)
                .setParameter("userId", userId)
                .setParameter("status", NotificationStatus.UNREAD)
                .setParameter("now", LocalDateTime.now())
                .getResultList();
    }

    /**
     * Get all notifications for user (with pagination)
     */
    public List<Notification> getAllNotificationsForUser(int userId, int offset) {
        return entityManager.createQuery(
                "SELECT n FROM Notification n "
                + "WHERE n.recipient.id = :userId "
                + "AND (n.expiresAt IS NULL OR n.expiresAt > :now) "
                + "ORDER BY n.status ASC, n.priority DESC, n.createdAt DESC",
                Notification.class)
                .setParameter("userId", userId)
                .setParameter("now", LocalDateTime.now())
                .setFirstResult(offset)
                .getResultList();
    }

    public List<Notification> getUnreadNotificationsForUser(int userId, int offset) {
        return entityManager.createQuery(
                "SELECT n FROM Notification n "
                + "WHERE n.recipient.id = :userId "
                + "AND n.status = :statuses "
                + "AND (n.expiresAt IS NULL OR n.expiresAt > :now) "
                + "ORDER BY n.createdAt DESC, n.status ASC, n.priority DESC",
                Notification.class)
                .setParameter("userId", userId)
                .setParameter("statuses", NotificationStatus.UNREAD)
                .setParameter("now", LocalDateTime.now())
                .setFirstResult(offset)
                .getResultList();
    }

    /**
     * Get unread count for user
     */
    public Long getUnreadCountForUser(int userId) {
        return entityManager.createQuery(
                "SELECT COUNT(n) FROM Notification n "
                + "WHERE n.recipient.id = :userId "
                + "AND n.status = :status "
                + "AND (n.expiresAt IS NULL OR n.expiresAt > :now)",
                Long.class)
                .setParameter("userId", userId)
                .setParameter("status", NotificationStatus.UNREAD)
                .setParameter("now", LocalDateTime.now())
                .getSingleResult();
    }

    // ===== UPDATE METHODS =====
    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(int notificationId, int userId) {
        Notification notification = entityManager.find(Notification.class, notificationId);
        if (notification != null && notification.getRecipient().getId() == (userId)) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
            entityManager.merge(notification);

            // Fire event for real-time count updates
            notificationEvent.fire(new NotificationEvent(notification, "READ"));
        }
    }

    /**
     * Mark all notifications as read for user
     */
    @Transactional
    public void markAllAsRead(int userId) {
        entityManager.createQuery(
                "UPDATE Notification n SET n.status = :readStatus, n.readAt = :readTime "
                + "WHERE n.recipient.id = :userId AND n.status = :unreadStatus")
                .setParameter("readStatus", NotificationStatus.READ)
                .setParameter("readTime", LocalDateTime.now())
                .setParameter("userId", userId)
                .setParameter("unreadStatus", NotificationStatus.UNREAD)
                .executeUpdate();

        notificationEvent.fire(new NotificationEvent(userId, "MARK_ALL_READ"));
    }

    /**
     * Delete/dismiss notification
     */
    @Transactional
    public void dismissNotification(int notificationId, int userId) {
        Notification notification = entityManager.find(Notification.class, notificationId);
        if (notification != null && notification.getRecipient().getId() == (userId)) {
            notification.setStatus(NotificationStatus.DISMISSED);
            entityManager.merge(notification);
        }
    }

    // ===== HELPER METHODS =====
    private void saveNotification(Notification notification) {
        entityManager.persist(notification);
    }

    private NotificationPriority mapSeverityToPriority(AnomalySeverity severity) {
        switch (severity) {
            case SEVERE:
                return NotificationPriority.URGENT;
            case WARNING:
                return NotificationPriority.HIGH;
            case INFO:
                return NotificationPriority.LOW;
            default:
                return NotificationPriority.NORMAL;
        }
    }

    private String getAnomalyIcon(AnomalySeverity severity, AnomalyStatus status) {
        if (status == AnomalyStatus.RESOLVED) {
            return "pi pi-check-circle text-green-500"; // PrimeIcons with green color
        }

        // fallback: use severity mapping
        switch (severity) {
            case SEVERE:
                return "pi pi-times-circle text-red-500";
            case WARNING:
                return "pi pi-exclamation-triangle text-orange-500";
            case INFO:
                return "pi pi-info-circle text-blue-500";
            default:
                return "pi pi-question-circle text-gray-400";
        }
    }

//    // ===== CLEANUP METHODS =====
//    /**
//     * Clean up expired notifications (scheduled job)
//     */
//    @Schedule(hour = "2", minute = "0") // Run daily at 2 AM
//    public void cleanupExpiredNotifications() {
//        int deleted = entityManager.createQuery(
//                "DELETE FROM Notification n WHERE n.expiresAt < :now")
//                .setParameter("now", LocalDateTime.now())
//                .executeUpdate();
//
//        logger.info("Cleaned up {} expired notifications", deleted);
//    }
}
