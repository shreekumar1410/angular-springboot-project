package com.example.registration.controller;

import com.example.registration.config.JwtUtil;
import com.example.registration.dto.*;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.service.AuthService;
import com.example.registration.service.LoginAuditService;
import com.example.registration.service.SupportPasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController extends BaseLogger {

    private final AuthService authService;
    private final UserAuthRepository userAuthRepository;
    private final LoginAuditService loginAuditService;
    private final JwtUtil jwtUtil;
    private final SupportPasswordResetService supportPasswordResetService;
    private static final String MESSAGE = "message";


    public AuthController(
            AuthService authService,
            UserAuthRepository userAuthRepository,
            LoginAuditService loginAuditService,
            JwtUtil jwtUtil,
            SupportPasswordResetService supportPasswordResetService) {

        this.authService = authService;
        this.userAuthRepository = userAuthRepository;
        this.loginAuditService = loginAuditService;
        this.jwtUtil = jwtUtil;
        this.supportPasswordResetService = supportPasswordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for email={}", request.getEmail());

        LoginResponse response = authService.login(request);

        log.info("Login successful for email={}", request.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request received for email={}", request.getEmail());

        authService.register(request);

        log.info("User registered successfully email={}", request.getEmail());

        return ResponseEntity.ok(
                Map.of(MESSAGE, "Registration successful")
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Logout called without valid Authorization header");
            return ResponseEntity.ok().build();
        }

        String jwt = header.substring(7);
        String email = jwtUtil.extractEmail(jwt);

        log.info("Logout request received for email={}", email);

        userAuthRepository.findByEmail(email).ifPresent(user ->
                loginAuditService.recordLogout(
                        user,
                        jwt,
                        jwtUtil.extractIssuedAt(jwt).toInstant(),
                        jwtUtil.extractExpiration(jwt).toInstant()
                )
        );

        log.info("Logout recorded successfully for email={}", email);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset-request")
    public Map<String, String> raiseRequest(
            @Valid @RequestBody ForgotPasswordRequest request) {

        log.info("Password reset request raised for email={}", request.getEmail());

        supportPasswordResetService.raiseRequest(request.getEmail());

        return Map.of(
                MESSAGE, "Password reset request submitted successfully"
        );
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Change password request received");

        authService.changePassword(request);

        log.info("Password changed successfully");

        return ResponseEntity.ok(
                Map.of(MESSAGE, "Password changed successfully")
        );
    }
}
