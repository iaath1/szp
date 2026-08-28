package com.stg.szp.DTO;

import com.stg.szp.models.ProjectRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String surname;
    private String bio;
    private String theme;
    private String accentColor;
    private boolean compactMode;
    private String avatarUrl;
    private ProjectRole projectRole;
    private NotificationsPreferencesDTO notifications;
    private boolean mfaEnabled;
}
