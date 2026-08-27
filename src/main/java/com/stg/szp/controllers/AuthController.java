package com.stg.szp.controllers;

import com.stg.szp.services.MfaService;
import java.sql.Timestamp;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.LoginUserDTO;
import com.stg.szp.DTO.RefreshTokenRequestDTO;
import com.stg.szp.DTO.RegisterUserDTO;
import com.stg.szp.models.LoginHistory;
import com.stg.szp.models.LoginStatus;
import com.stg.szp.models.SZP_User;
import com.stg.szp.models.UserSessions;
import com.stg.szp.repos.LoginHistoryRepository;
import com.stg.szp.repos.UserSessionsRepository;
import com.stg.szp.services.AuthService;
import com.stg.szp.services.JwtService;
import com.stg.szp.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MfaService mfaService;
    private final JwtService jwtService;
    private final AuthService authService;
    private final UserService userService;
    private final UserSessionsRepository sessionRepo;
    private final LoginHistoryRepository loginHistoryRepo;

    @Value("${security.jwt.refresh-expiration-time}")
    private Long jwtRefreshExpiration;
    
    @PostMapping("/register")
    public ResponseEntity<SZP_User> register(@RequestBody RegisterUserDTO registerUserDTO) {
        
        SZP_User registeredUser = authService.signup(registerUserDTO);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDTO loginUserDTO, HttpServletRequest request) {

        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setAttemptTime(new Timestamp(System.currentTimeMillis()));
        loginHistory.setDeviceInfo(request.getHeader("User-Agent"));
        loginHistory.setEmail(loginUserDTO.getEmail());
        loginHistory.setIpAddress(request.getRemoteAddr());

        try {
            SZP_User authenticatedUser = authService.authenticate(loginUserDTO);

            if(authenticatedUser.isMfaEnabled()) {
                return ResponseEntity.accepted().body(Map.of("message", "Enter your secret key."));
            }

            String jwtToken = jwtService.generateAccessToken(authenticatedUser);
            String refreshToken = jwtService.generateRefreshToken(authenticatedUser);
            // authenticatedUser.setRefreshToken(refreshToken);
            // userService.saveUser(authenticatedUser);
            String deviceInfo = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();

            UserSessions session = sessionRepo.findByUserIdAndDeviceInfoAndIpAddress(authenticatedUser.getId(),
            deviceInfo,
            ipAddress).orElse(new UserSessions());

            session.setUser(authenticatedUser);
            session.setRefreshToken(refreshToken);
            session.setIpAddress(ipAddress);
            session.setDeviceInfo(deviceInfo);
            session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            session.setExpiresAt(new Timestamp(System.currentTimeMillis() + jwtRefreshExpiration));

            sessionRepo.save(session);

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setFirstname(authenticatedUser.getName());
            loginResponse.setLastname(authenticatedUser.getSurname());
            loginResponse.setAccessToken(jwtToken);
            loginResponse.setExpiration(jwtService.getJwtExpiration());
            loginResponse.setRefreshToken(authenticatedUser.getRefreshToken());
            loginResponse.setAvatarUrl(authenticatedUser.getAvatarPath());

            loginHistory.setStatus(LoginStatus.SUCCESS);
            loginHistoryRepo.save(loginHistory);

            return ResponseEntity.ok(loginResponse);
        } catch(UsernameNotFoundException ex) {
            loginHistory.setStatus(LoginStatus.FAILED_PASSWORD);
            loginHistoryRepo.save(loginHistory);

            return ResponseEntity.badRequest().body(Map.of("messgae", "Email or password is invalid."));
        }
        
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        return authService.refresh(refreshTokenRequest.getRefreshToken());
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<?> loginWith2fa(@RequestBody LoginUserDTO loginUserDTO, @RequestParam String code, HttpServletRequest request) {

        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setAttemptTime(new Timestamp(System.currentTimeMillis()));
        loginHistory.setEmail(loginUserDTO.getEmail());
        loginHistory.setIpAddress(request.getRemoteAddr());
        loginHistory.setDeviceInfo(request.getHeader("User-Agent"));

        SZP_User authenticatedUser = authService.authenticate(loginUserDTO);

        if(!mfaService.verifyCode(authenticatedUser.getMfaSecret(), code)) {
            loginHistory.setStatus(LoginStatus.MFA_FAILED);
            loginHistoryRepo.save(loginHistory);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid 2FA code"));

        }

        String jwtToken = jwtService.generateAccessToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);
        String deviceInfo = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();

        UserSessions session = sessionRepo.findByUserIdAndDeviceInfoAndIpAddress(authenticatedUser.getId(),
        deviceInfo,
        ipAddress).orElse(new UserSessions());

        session.setUser(authenticatedUser);
        session.setRefreshToken(refreshToken);
        session.setIpAddress(ipAddress);
        session.setDeviceInfo(deviceInfo);
        session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        session.setExpiresAt(new Timestamp(System.currentTimeMillis() + jwtRefreshExpiration));

        sessionRepo.save(session);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setFirstname(authenticatedUser.getName());
        loginResponse.setLastname(authenticatedUser.getSurname());
        loginResponse.setAccessToken(jwtToken);
        loginResponse.setExpiration(jwtService.getJwtExpiration());
        loginResponse.setRefreshToken(authenticatedUser.getRefreshToken());
        loginResponse.setAvatarUrl(authenticatedUser.getAvatarPath());

        loginHistory.setStatus(LoginStatus.SUCCESS);
        loginHistoryRepo.save(loginHistory);

        return ResponseEntity.ok(loginResponse);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    class LoginResponse {
        private String firstname;
        private String lastname;
        private String accessToken;
        private String refreshToken;
        private long expiration;
        private String avatarUrl;
    }

}
