package com.stg.szp.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.ActivityLogDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.ActivityLogService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/activities")
@AllArgsConstructor
public class ActivityLogController {
    
    private final ActivityLogService activityService;

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityLogDTO>> getRecentActivities(
        @AuthenticationPrincipal SZP_User user,
        @RequestParam(defaultValue = "10") int limit
    ) {
        if(user == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<ActivityLogDTO> response = activityService.getRecentActivities(user, limit);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
