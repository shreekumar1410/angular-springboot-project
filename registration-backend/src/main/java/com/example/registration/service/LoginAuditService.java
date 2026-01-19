package com.example.registration.service;

import com.example.registration.dto.LoginAlertDTO;
import com.example.registration.entity.LoginAudit;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.LoginAlertType;
import com.example.registration.enums.LoginReason;
import com.example.registration.enums.LoginType;
import com.example.registration.repository.LoginAuditRepository;
import com.example.registration.security.JwtHashUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class LoginAuditService {

    private final LoginAuditRepository loginAuditRepository;

    public LoginAuditService(LoginAuditRepository loginAuditRepository) {
        this.loginAuditRepository = loginAuditRepository;
    }

    public List<LoginAudit> getAllAudits() {
        return loginAuditRepository.findAll();
    }

    public void recordLogin(UserAuth user, String jwt, Instant issuedAt, Instant expiresAt) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(user.getEmail());
            audit.setUserAuth(user);
            audit.setRole(user.getRole());
            audit.setEventTime(Instant.now());
            audit.setLoginType(LoginType.LOGIN);
            audit.setReason(LoginReason.LOGIN_SUCCESS);
            audit.setJwtTokenHash(JwtHashUtil.hash(jwt));
            audit.setJwtIssuedAt(issuedAt);
            audit.setJwtExpiresAt(expiresAt);
            loginAuditRepository.save(audit);
        } catch (Exception ignored) {}
    }

    public void recordLogout(UserAuth user, String jwt, Instant issuedAt, Instant expiresAt) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(user.getEmail());
            audit.setUserAuth(user);
            audit.setRole(user.getRole());
            audit.setEventTime(Instant.now());
            audit.setLoginType(LoginType.LOGOUT);
            audit.setReason(LoginReason.USER_LOGOUT);
            audit.setJwtTokenHash(JwtHashUtil.hash(jwt));
            audit.setJwtIssuedAt(issuedAt);
            audit.setJwtExpiresAt(expiresAt);
            loginAuditRepository.save(audit);
        } catch (Exception ignored) {}
    }

    public void recordFailure(String email, LoginReason reason) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(email);
            audit.setEventTime(Instant.now());
            audit.setLoginType(LoginType.FAILED);
            audit.setReason(reason);
            loginAuditRepository.save(audit);
        } catch (Exception ignored) {}
    }

    public LoginAlertDTO buildLoginAlert(String email, Instant currentLoginTime) {

        List<LoginAudit> logins =
                loginAuditRepository.findByEmailAndLoginTypeOrderByEventTimeAsc(
                        email,
                        LoginType.LOGIN
                );

        // 🟢 FIRST LOGIN
        if (logins.size() == 1) {
            return new LoginAlertDTO(
                    LoginAlertType.FIRST_LOGIN,
                    null,
                    "Welcome! This is your first login to the system."
            );
        }

        // Find last logout
        return loginAuditRepository
                .findTopByEmailAndLoginTypeOrderByEventTimeDesc(
                        email,
                        LoginType.LOGOUT
                )
                .map(logout -> {
                    Duration diff = Duration.between(
                            logout.getEventTime(),
                            currentLoginTime
                    );

                    String formattedDiff = formatDuration(diff);

                    return new LoginAlertDTO(
                            LoginAlertType.NORMAL,
                            logout.getEventTime(),
                            "Welcome back! You last logged out " + formattedDiff + " ."
                    );
                })
                .orElseGet(() ->
                        new LoginAlertDTO(
                                LoginAlertType.SESSION_TIMEOUT,
                                null,
                                "Welcome back! Your last session ended without a proper logout."
                        )
                );
    }

    private String formatDuration(Duration duration) {

        long totalMinutes = duration.toMinutes();

        long days = totalMinutes / (24 * 60);
        long hours = (totalMinutes % (24 * 60)) / 60;
        long minutes = totalMinutes % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(days == 1 ? " day " : " days ");
        }
        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hour " : " hours ");
        }
        if (minutes > 0 || result.length() == 0) {
            result.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        }

        result.append("ago");

        return result.toString().trim();
    }

    public List<LoginAudit> getCurrentUserAudit() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        return loginAuditRepository.findByEmail(email);
    }

    public void recordPasswordChange(
            String email,
            String role,
            boolean success,
            LoginReason reason
    ) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(email);
            audit.setRole(role);
            audit.setLoginType(LoginType.PASSWORD_CHANGED);
            audit.setEventTime(Instant.now());
            audit.setReason(reason);

            loginAuditRepository.save(audit);
        } catch (Exception ignored) {}
    }


}
