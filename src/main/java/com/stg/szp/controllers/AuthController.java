package com.stg.szp.controllers;

import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.LoginUserDTO;
import com.stg.szp.DTO.RefreshTokenRequestDTO;
import com.stg.szp.DTO.RegisterUserDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.AuthService;
import com.stg.szp.services.JwtService;
import com.stg.szp.services.UserService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;
    private final UserService userService;

    public AuthController(JwtService jwtService, AuthService authService, UserService userService) {
        this.jwtService = jwtService;
        this.authService = authService;
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<SZP_User> register(@RequestBody RegisterUserDTO registerUserDTO) {
        
        SZP_User registeredUser = authService.signup(registerUserDTO);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDTO loginUserDTO) {
        SZP_User authenticatedUser = authService.authenticate(loginUserDTO);

        String jwtToken = jwtService.generateAccessToken(authenticatedUser);
        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);
        authenticatedUser.setRefreshToken(refreshToken);
        userService.saveUser(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setFirstname(authenticatedUser.getName());
        loginResponse.setLastname(authenticatedUser.getSurname());
        loginResponse.setAccessToken(jwtToken);
        loginResponse.setExpiration(jwtService.getJwtExpiration());
        loginResponse.setRefreshToken(authenticatedUser.getRefreshToken());
        loginResponse.setAvatarUrl(authenticatedUser.getAvatarPath());
        
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequest) {
        return authService.refresh(refreshTokenRequest.getRefreshToken());
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
