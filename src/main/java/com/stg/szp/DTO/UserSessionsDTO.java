package com.stg.szp.DTO;



import java.sql.Timestamp;

import com.stg.szp.models.SZP_User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSessionsDTO {
    private Long id;
    private String userEmail;
    private String refreshToken;
    private String ipAddress;
    private String deviceInfo;
    private String location;

    private Timestamp createdAt;
    private Timestamp expiresAt;
}
