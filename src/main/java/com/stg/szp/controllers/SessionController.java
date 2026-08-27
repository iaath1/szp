package com.stg.szp.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.UserSessionsDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.UserSessions;
import com.stg.szp.repos.UserSessionsRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/users/sessions")
@AllArgsConstructor
public class SessionController {
    private final UserSessionsRepository sessionRepo;

    @GetMapping
    public ResponseEntity<List<UserSessionsDTO>> getActiveSessions(@AuthenticationPrincipal SZP_User user, HttpServletRequest request) {
        List<UserSessions> userSessions = sessionRepo.findAllByUserIdAndExpiresAtAfter(user.getId(), new Timestamp(System.currentTimeMillis()));
        
        List<UserSessionsDTO> response = userSessions.stream().map(
            session -> mapToSessionDto(session)
        ).toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> revokeSession(@AuthenticationPrincipal SZP_User user, @PathVariable Long sessionId) {
        sessionRepo.deleteByIdAndUserId(sessionId, user.getId());
        return ResponseEntity.ok().build();
    }
    
    private UserSessionsDTO mapToSessionDto(UserSessions session) {
        return UserSessionsDTO.builder()
            .createdAt(session.getCreatedAt())
            .expiresAt(session.getExpiresAt())
            .deviceInfo(session.getDeviceInfo())
            .ipAddress(session.getIpAddress())
            .id(session.getId())
            .location(session.getLocation())
            .refreshToken(session.getRefreshToken())
            .userEmail(session.getUser().getEmail())
            .build();
    }
}
