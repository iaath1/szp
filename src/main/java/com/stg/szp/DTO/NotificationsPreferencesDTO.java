package com.stg.szp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotificationsPreferencesDTO {
    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean taskUpdated;
    private boolean projectInvites;
    private boolean mentions;
}
