package com.stg.szp.controllers;

import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stg.szp.DTO.ChangePasswordDTO;
import com.stg.szp.DTO.NotificationsPreferencesDTO;
import com.stg.szp.DTO.UserProfileUpdateDTO;
import com.stg.szp.DTO.UserResponseDTO;
import com.stg.szp.models.NotificationPreferences;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.UserService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/suggested/{projectId}")
    public ResponseEntity<List<UserResponseDTO>> getSuggestedUsers(@AuthenticationPrincipal SZP_User user, @PathVariable Long projectId) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        List<UserResponseDTO> response = userService.getSuggestedUsers(user, projectId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/team")
    public ResponseEntity<List<UserResponseDTO>> getMyTeam(@AuthenticationPrincipal SZP_User user) {
        if(user == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        List<UserResponseDTO> response = userService.getMyTeam(user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getMyProfile(@AuthenticationPrincipal SZP_User user) {
        if(user == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        
        UserResponseDTO response = userService.mapToUserResponseDTO(user);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateUserProfile(@AuthenticationPrincipal SZP_User user, @RequestBody UserProfileUpdateDTO dto) {
        if(user == null || !user.getEmail().equals(dto.getEmail())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        UserResponseDTO response = userService.updateUser(user, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/profile/avatar")
    public ResponseEntity<UserResponseDTO> updateUserAvatar(@AuthenticationPrincipal SZP_User user, @RequestBody MultipartFile file) {
        if(user == null || file.isEmpty()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        UserResponseDTO response = userService.uploadNewAvatar(user, file);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile/notifications")
    public ResponseEntity<UserResponseDTO> updateNotificationPreferences(@AuthenticationPrincipal SZP_User user,@RequestBody NotificationsPreferencesDTO dto) {
        if(user == null || dto == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        UserResponseDTO response = userService.updateNotifications(user, dto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/profile/password")
    public ResponseEntity<?> changeUserPassword(@AuthenticationPrincipal SZP_User user, @RequestBody ChangePasswordDTO dto) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        try {
            userService.changePassword(user, dto);
            return ResponseEntity.ok().build();
        } catch(IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
