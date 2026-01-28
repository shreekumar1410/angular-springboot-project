package com.example.registration.service;

import com.example.registration.dto.*;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.LoginReason;
import com.example.registration.enums.Roles;
import com.example.registration.exception.BadRequestException;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.config.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService extends BaseLogger {

    private final UserAuthRepository authRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginAuditService loginAuditService;

    public AuthService(UserAuthRepository authRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       LoginAuditService loginAuditService) {
        this.authRepo = authRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginAuditService = loginAuditService;
    }

    // =====================================================
    // 🔐 LOGIN
    // =====================================================

    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for email={}", request.getEmail());

        UserAuth auth = authRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - email not found email={}", request.getEmail());
                    loginAuditService.recordFailure(
                            request.getEmail(),
                            LoginReason.EMAIL_NOT_FOUND
                    );
                    return new BadRequestException("Invalid email");
                });

        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {

            log.warn("Login failed - invalid password email={}", request.getEmail());

            loginAuditService.recordFailure(
                    request.getEmail(),
                    LoginReason.INVALID_PASSWORD
            );
            throw new BadRequestException("Invalid password");
        }

        if (!auth.isActive()) {

            log.warn("Login failed - account disabled email={}", request.getEmail());

            loginAuditService.recordFailure(
                    request.getEmail(),
                    LoginReason.USER_DISABLED
            );
            throw new BadRequestException("Account is disabled");
        }

        String token = jwtUtil.generateToken(
                auth.getEmail(),
                auth.getRole()
        );

        Instant loginTime = Instant.now();

        loginAuditService.recordLogin(
                auth,
                token,
                jwtUtil.extractIssuedAt(token).toInstant(),
                jwtUtil.extractExpiration(token).toInstant()
        );

        LoginAlertDTO loginAlert =
                loginAuditService.buildLoginAlert(
                        auth.getEmail(),
                        loginTime
                );

        log.info("Login successful for email={} role={}",
                auth.getEmail(), auth.getRole());

        return new LoginResponse(
                token,
                auth.getRole(),
                auth.isProfileCreated(),
                loginAlert
        );
    }

    // =====================================================
    // 📝 REGISTER
    // =====================================================

    public UserAuth register(RegisterRequest request) {

        log.info("Registration attempt for email={}", request.getEmail());

        if (authRepo.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed - email already exists email={}", request.getEmail());
            throw new BadRequestException("Email already registered");
        }

        UserAuth auth = new UserAuth();
        auth.setEmail(request.getEmail());
        auth.setPassword(passwordEncoder.encode(request.getPassword()));
        auth.setRole(
                request.getRole() != null ? request.getRole() : Roles.USER
        );
        auth.setProfileCreated(false);
        auth.setActive(true);

        UserAuth saved = authRepo.save(auth);

        log.info("Registration successful email={} role={}",
                saved.getEmail(), saved.getRole());

        return saved;
    }

    // =====================================================
    // 🔑 CHANGE PASSWORD
    // =====================================================

    public void changePassword(ChangePasswordRequest request) {

        var logAuth = SecurityContextHolder.getContext().getAuthentication();
        if (logAuth == null || !logAuth.isAuthenticated() || logAuth.getAuthorities().isEmpty()) {
            throw new BadRequestException("Unauthenticated request");
        }


        String email = logAuth.getName();

        String role = logAuth.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        log.info("Password change attempt email={} role={}", email, role);

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                auth.getPassword()
        )) {

            log.warn("Password change failed - invalid current password email={}", email);

            loginAuditService.recordPasswordChange(
                    email,
                    role,
                    false,
                    LoginReason.INVALID_CURRENT_PASSWORD
            );
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                auth.getPassword()
        )) {

            log.warn("Password change failed - password reuse email={}", email);

            loginAuditService.recordPasswordChange(
                    email,
                    role,
                    false,
                    LoginReason.SAME_PASSWORD_REUSE
            );
            throw new BadRequestException("New password must be different");
        }

        auth.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        authRepo.save(auth);

        loginAuditService.recordPasswordChange(
                email,
                role,
                true,
                LoginReason.PASSWORD_CHANGED_SUCCESS
        );

        log.info("Password changed successfully email={}", email);
    }
}
