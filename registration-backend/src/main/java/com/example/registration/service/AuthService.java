package com.example.registration.service;

import com.example.registration.dto.*;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.LoginReason;
import com.example.registration.enums.Roles;
import com.example.registration.exception.BadRequestException;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.config.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

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

    public LoginResponse login(LoginRequest request) {

        UserAuth auth = authRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    loginAuditService.recordFailure(
                            request.getEmail(),
                            LoginReason.EMAIL_NOT_FOUND
                    );
                    return new BadRequestException("Invalid email");
                });

        if (!passwordEncoder.matches(request.getPassword(), auth.getPassword())) {
            loginAuditService.recordFailure(
                    request.getEmail(),
                    LoginReason.INVALID_PASSWORD
            );
            throw new BadRequestException("Invalid password");
        }

        if (!auth.isActive()) {
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

//        audit.setFailureReason(reason);

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

        return new LoginResponse(
                token,
                auth.getRole(),
                auth.isProfileCreated(),
                loginAlert
        );
    }

    public UserAuth register(RegisterRequest request) {

        if (authRepo.findByEmail(request.getEmail()).isPresent()) {
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

        return authRepo.save(auth);
    }

    public void changePassword(ChangePasswordRequest request) {

        // 1. Get logged-in user's email from JWT
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        String role = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Validate current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                auth.getPassword()
        )) {
            loginAuditService.recordPasswordChange(
                    email,
                    role,
                    false,
                    LoginReason.INVALID_CURRENT_PASSWORD
            );
            throw new BadRequestException("Current password is incorrect");
        }

        // 3. Prevent same password reuse
        if (passwordEncoder.matches(
                request.getNewPassword(),
                auth.getPassword()
        )){
            loginAuditService.recordPasswordChange(
                    email,
                    role,
                    false,
                    LoginReason.SAME_PASSWORD_REUSE
            );
            throw new BadRequestException("New password must be different");
        }

        // 4. Update password
        auth.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        authRepo.save(auth);

        // ✅ audit success
        loginAuditService.recordPasswordChange(
                email,
                role,
                true,
                LoginReason.PASSWORD_CHANGED_SUCCESS
        );
    }


}
