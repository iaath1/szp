package com.stg.szp.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stg.szp.DTO.LoginUserDTO;
import com.stg.szp.DTO.RegisterUserDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.services.AuthService;
import com.stg.szp.services.JwtService;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtService jwtService;
    private final AuthService authService;

    public AuthController(JwtService jwtService, AuthService authService) {
        this.jwtService = jwtService;
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<SZP_User> register(@RequestBody RegisterUserDTO registerUserDTO) {
        
        SZP_User registeredUser = authService.signup(registerUserDTO);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginUserDTO loginUserDTO) {
        SZP_User authenticatedUser = authService.authenticate(loginUserDTO);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setExpiration(jwtService.getJwtExpiration());

        return ResponseEntity.ok(loginResponse);
    }


    @Getter
    @Setter
    @NoArgsConstructor
    class LoginResponse {
        private String token;
        private long expiration;
    }

}
