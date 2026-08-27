package com.stg.szp.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.SZP_UserRepository;
import com.stg.szp.services.MfaService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/users/2fa")
@AllArgsConstructor
public class MfaController {

    private final MfaService mfaService;
    private final SZP_UserRepository userRepo;
    
    @GetMapping("/setup")
    public ResponseEntity<?> setup2FA(@AuthenticationPrincipal SZP_User user) throws Exception {
        String secret = mfaService.generateSecret();
        user.setMfaSecret(secret);
        userRepo.save(user);

        String qrCodeUrl = mfaService.getQrCodeImage(secret, user.getEmail());
        return ResponseEntity.ok(Map.of("qrCode", qrCodeUrl, "secret", secret));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAndEnable(@AuthenticationPrincipal SZP_User user,
        @RequestBody Map<String, String> body
    ) {
        String code = body.get("code");
        if(mfaService.verifyCode(user.getMfaSecret(), code)) {
            user.setMfaEnabled(true);
            userRepo.save(user);

            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid password"));
    }
}
