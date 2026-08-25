package com.stg.szp.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.stg.szp.models.Notification;
import com.stg.szp.models.NotificationDTO;
import com.stg.szp.models.NotificationPreferences;
import com.stg.szp.models.NotificationType;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.NotificationRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepo;
    private final SimpMessagingTemplate messagingTemplate;

    public void createNotification(SZP_User recipient, NotificationType type, String title, String message, String link) {
        boolean isAllowed = false;
        NotificationPreferences preferences = recipient.getNotificationPreferences();
        if(preferences == null) isAllowed = true;
        else {
            switch (type) {
                case MENTION:
                    isAllowed = preferences.isMentions();
                    break;
                case PROJECT_INVITE:
                    isAllowed = preferences.isProjectInvites();
                    break;
                case TASK_UPDATE:
                    isAllowed = preferences.isTaskUpdated();
                    break;
                case SYSTEM:
                default:
                    isAllowed = true;
                    break;
            }
        }

        if(!isAllowed) return;

        Notification notification = new Notification();
            notification.setCreatedAt(LocalDateTime.now());
            notification.setLink(!link.isBlank() ? link : null);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setTitle(title);
            notification.setType(type);
            notification.setRecipient(recipient);

        notificationRepo.save(notification);

        NotificationDTO dto = mapToNotificationDTO(notification);
        messagingTemplate.convertAndSend(
            "/topic/notifications/" + recipient.getEmail(), 
            dto
        );
    }

    public List<NotificationDTO> getMyNotifications(SZP_User user) {
        return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(user.getId()).stream().map(
            this::mapToNotificationDTO
        ).collect(Collectors.toList());
    }

    public Long getUnreadCount(SZP_User user) {
        return notificationRepo.countByRecipientIdAndIsReadFalse(user.getId());
    }

    public void markAsRead(Long id, SZP_User user) {
        Notification notification = notificationRepo.findById(id).orElse(null);
        if(notification != null && notification.getRecipient().getId().equals(user.getId())) {
            notification.setRead(true);
            notificationRepo.save(notification);
        }
    }

    public void markAllAsRead(SZP_User user) {
        List<Notification> unread = notificationRepo.findAllByRecipientIdAndIsReadFalse(user.getId());
        for(Notification not : unread) {
            not.setRead(true);
        }

        notificationRepo.saveAll(unread);
    }

    private NotificationDTO mapToNotificationDTO(Notification notification) {
        return NotificationDTO.builder()
            .id(notification.getId())
            .type(notification.getType())
            .createdAt(notification.getCreatedAt())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .isRead(notification.isRead())
            .link(notification.getLink())
            .build();
    }
}
