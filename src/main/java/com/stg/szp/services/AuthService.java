package com.stg.szp.services;

import com.stg.szp.repos.RoleRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.security.autoconfigure.SecurityProperties.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.stg.szp.DTO.LoginUserDTO;
import com.stg.szp.DTO.RegisterUserDTO;
import com.stg.szp.models.SZP_User;
import com.stg.szp.repos.SZP_UserRepository;

@Service
public class AuthService {
    private final RoleRepository roleRepository;
    private final SZP_UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthService(SZP_UserRepository userRepo,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authManager,
        RoleRepository roleRepository,
        JwtService jwtService
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
    }

    public SZP_User signup(RegisterUserDTO input) {
        SZP_User user = new SZP_User();
        user.setName(input.getName());
        user.setSurname(input.getSurname());
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRoles(Set.of(roleRepository.findByRoleName("ROLE_USER").get()));

        return userRepo.save(user);
    }

    public SZP_User authenticate(LoginUserDTO input) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );

        return userRepo.findByEmail(input.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + input.getEmail()));
    }

    public ResponseEntity<?> refresh(String refreshToken) {
        if(refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body("Refresh token is invalid");
        }

        String username = jwtService.extractUsername(refreshToken);

        SZP_User user = userRepo.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found."));

        if(!refreshToken.equals(user.getRefreshToken())) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        if(!jwtService.isTokenValid(refreshToken, user)) {
            return ResponseEntity.status(401).body("Refresh token has been expired");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

    }

    public SZP_User authenticateWithGoogle(String email, String name, String familyName, String pictureUrl) {
        Optional<SZP_User> userOpt = userRepo.findByEmail(email);

        if(userOpt.isPresent()) {
            SZP_User user = userOpt.get();
            return user;
        } else {
            SZP_User user = new SZP_User();
            user.setEmail(email);
            user.setName(name);
            user.setSurname(familyName != null ? familyName : "");
            user.setAvatarPath(pictureUrl);

            // can be added in the future
            // user.setAuthProvider("GOOGLE")

            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

            return userRepo.save(user);
        }
    }

    public SZP_User authenticateWithGithub(String email, String name, String login, String avatarUrl) {
        Optional<SZP_User> userOpt = userRepo.findByEmail(email);
        if(userOpt.isPresent()) {
            return userOpt.get();
        } else {
            SZP_User newUser = new SZP_User();
            newUser.setEmail(email);

            if(name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ", 2);
                newUser.setName(nameParts[0]);
                if(nameParts.length > 1) {
                    newUser.setSurname(nameParts[1]);
                } else {
                    newUser.setSurname("");
                }
            } else {
                newUser.setName(login);
                newUser.setSurname("");
            }

            newUser.setAvatarPath(avatarUrl);
            newUser.setMfaEnabled(false);

            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            return userRepo.save(newUser);
        }
    }
}
