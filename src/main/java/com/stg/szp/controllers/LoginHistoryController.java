package com.stg.szp.controllers;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.models.LoginHistory;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.LoginHistoryRepository;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users/login-history")
@AllArgsConstructor
public class LoginHistoryController {
    private final LoginHistoryRepository loginHistoryRepo;

    @GetMapping
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal SZP_User user) {
        List<LoginHistory> history = loginHistoryRepo.findTop20ByEmailOrderByAttemptTimeDesc(user.getEmail());
        return ResponseEntity.ok(history);
    }
}
