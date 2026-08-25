package com.stg.szp.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.models.NotificationDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.NotificationService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(@AuthenticationPrincipal SZP_User user) {

        // In other controllers need to be changed
        if (user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        
        List<NotificationDTO> response = notificationService.getMyNotifications(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        long count = notificationService.getUnreadCount(user);
        return new ResponseEntity<>(Map.of("count", count), HttpStatus.OK);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal SZP_User user, @PathVariable Long id) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        notificationService.markAsRead(id, user);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal SZP_User user) {
        if(user ==  null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        notificationService.markAllAsRead(user);
        return ResponseEntity.ok().build();
    }
}
