package com.stg.szp.models;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPreferences {
    private boolean emailNotifications = true;
    private boolean pushNotifications = true;
    private boolean taskUpdated = true;
    private boolean projectInvites = true;
    private boolean mentions = true;
}
