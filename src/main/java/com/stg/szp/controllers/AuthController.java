package com.stg.szp.controllers;

import com.stg.szp.services.MfaService;


import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.catalina.connector.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.stg.szp.DTO.GithubCodeDTO;
import com.stg.szp.DTO.GoogleTokenDTO;
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

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.secret}")
    private String githubSecret;
    
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

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleTokenDTO googleToken, HttpServletRequest request) {

        System.out.println(googleToken.getToken());

        if(googleToken == null || googleToken.getToken() == null || googleToken.getToken().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Token is missing or empty"));
        }

        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setAttemptTime(new Timestamp(System.currentTimeMillis()));
        loginHistory.setDeviceInfo(request.getHeader("User-Agent"));
        loginHistory.setIpAddress(request.getRemoteAddr());
        
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

            GoogleIdToken idToken = verifier.verify(googleToken.getToken());
            if(idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("given_name");
                String familyName = (String) payload.get("family_name");
                String pictureUrl = (String) payload.get("picture");

                loginHistory.setEmail(email);

                SZP_User authenticatedUser = authService.authenticateWithGoogle(email, name, familyName, pictureUrl);

                String jwtToken = jwtService.generateAccessToken(authenticatedUser);
                String refreshToken = jwtService.generateRefreshToken(authenticatedUser);

                UserSessions session = sessionRepo.findByUserIdAndDeviceInfoAndIpAddress(authenticatedUser.getId(),
                request.getHeader("User-Agent"),
                request.getRemoteAddr()).orElse(new UserSessions());

                session.setUser(authenticatedUser);
                session.setRefreshToken(refreshToken);
                session.setIpAddress(request.getRemoteAddr());
                session.setDeviceInfo(request.getHeader("User-Agent"));
                session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                session.setExpiresAt(new Timestamp(System.currentTimeMillis() + jwtRefreshExpiration));

                LoginResponse response = new LoginResponse();
                response.setAccessToken(jwtToken);
                response.setRefreshToken(refreshToken);
                response.setFirstname(authenticatedUser.getName());
                response.setLastname(authenticatedUser.getSurname());
                response.setAvatarUrl(authenticatedUser.getAvatarPath());
                response.setExpiration(jwtService.getJwtExpiration());
                
                loginHistory.setStatus(LoginStatus.SUCCESS);
                loginHistoryRepo.save(loginHistory);

                sessionRepo.save(session);

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                loginHistory.setStatus(LoginStatus.FAILED_PASSWORD);
                loginHistoryRepo.save(loginHistory);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid Google ID token"));
            }
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Server error during Google validation"));
        }
    }

    @PostMapping("/github")
    public ResponseEntity<?> loginWithGithub(@RequestBody GithubCodeDTO dto, HttpServletRequest request) {
        if(dto == null || dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Github code is missing"));
        }

        LoginHistory loginHistory = new LoginHistory();
        loginHistory.setAttemptTime(new Timestamp(System.currentTimeMillis()));
        loginHistory.setDeviceInfo(request.getHeader("User-Agent"));
        loginHistory.setIpAddress(request.getRemoteAddr());

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            Map<String, String> body = new HashMap<>();
            body.put("client_id", githubClientId);
            body.put("client_secret", githubSecret);
            body.put("code", dto.getCode());

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity("https://github.com/login/oauth/access_token", requestEntity, Map.class);
            String githubAccessToken = (String) tokenResponse.getBody().get("access_token");

            if(githubAccessToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Failed to get Github access token"));
            }

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.setBearerAuth(githubAccessToken);
            HttpEntity<Void> userReqeustEntity = new HttpEntity<>(userHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                userReqeustEntity,
                Map.class
            );

            Map<String, Object> userData = userResponse.getBody();
            String login = (String) userData.get("login");
            String name = (String) userData.get("name");
            String email = (String) userData.get("email");
            String avatarUrl = (String) userData.get("avatarUrl");

            if(email == null) {
                ResponseEntity<List> emailResponse = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    userReqeustEntity,
                    List.class
                );

                List<Map<String, Object>> emailsList = emailResponse.getBody();
                if(emailsList != null) {
                    for (Map<String, Object> emailObj : emailsList) {
                        Boolean primary = (Boolean) emailObj.get("primary");
                        if(primary != null && primary) {
                            email = (String) emailObj.get("email");
                            break;
                        }
                    }
                }
            }

            if(email == null || email.isEmpty()) {
                loginHistory.setStatus(LoginStatus.FAILED_PASSWORD);
                loginHistoryRepo.save(loginHistory);
                return ResponseEntity.badRequest().body(Map.of("message", "Github email is required but not provided"));
            }

            loginHistory.setEmail(email);

            // Email may be null, needs to be added one more request https://api.github.com/user/emails to get hided user email

            SZP_User authenticatedUser = authService.authenticateWithGithub(email, name, login, avatarUrl);

            String jwtToken = jwtService.generateAccessToken(authenticatedUser);
            String refreshToken = jwtService.generateRefreshToken(authenticatedUser);

            UserSessions session = sessionRepo.findByUserIdAndDeviceInfoAndIpAddress(authenticatedUser.getId(),
            request.getHeader("User-Agent"),
            request.getRemoteAddr()).orElse(new UserSessions());

            session.setUser(authenticatedUser);
            session.setRefreshToken(refreshToken);
            session.setIpAddress(request.getRemoteAddr());
            session.setDeviceInfo(request.getHeader("User-Agent"));
            session.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            session.setExpiresAt(new Timestamp(System.currentTimeMillis() + jwtRefreshExpiration));

            LoginResponse response = new LoginResponse();
            response.setAccessToken(jwtToken);
            response.setRefreshToken(refreshToken);
            response.setFirstname(authenticatedUser.getName());
            response.setLastname(authenticatedUser.getSurname());
            response.setAvatarUrl(authenticatedUser.getAvatarPath());
            response.setExpiration(jwtService.getJwtExpiration());
            
            
            loginHistory.setStatus(LoginStatus.SUCCESS);
            loginHistoryRepo.save(loginHistory);

            sessionRepo.save(session);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Github Auth Failed"));
        }
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
