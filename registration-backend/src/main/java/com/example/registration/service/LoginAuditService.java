package com.example.registration.service;

import com.example.registration.dto.LoginAlertDTO;
import com.example.registration.entity.LoginAudit;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.LoginAlertType;
import com.example.registration.enums.LoginReason;
import com.example.registration.enums.LoginType;
import com.example.registration.exception.BadRequestException;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.LoginAuditRepository;
import com.example.registration.security.JwtHashUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class LoginAuditService extends BaseLogger {

    private final LoginAuditRepository loginAuditRepository;

    public LoginAuditService(LoginAuditRepository loginAuditRepository) {
        this.loginAuditRepository = loginAuditRepository;
    }

    public List<LoginAudit> getAllAudits() {

        log.info("Login audit list requested");

        return loginAuditRepository.findAll();
    }

    public void recordLogin(UserAuth user, String jwt, Instant issuedAt, Instant expiresAt) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(user.getEmail());
            audit.setUserAuth(user);
            audit.setRole(user.getRole().name());
            audit.setEventTime(Instant.now());
            audit.setLoginType(LoginType.LOGIN);
            audit.setReason(LoginReason.LOGIN_SUCCESS);
            audit.setJwtTokenHash(JwtHashUtil.hash(jwt));
            audit.setJwtIssuedAt(issuedAt);
            audit.setJwtExpiresAt(expiresAt);

            loginAuditRepository.save(audit);

            log.info("Login audit recorded email={} role={}", user.getEmail(), user.getRole());

        } catch (RuntimeException ex) {
            // Audit must never break login
            log.error("Failed to record login audit for email={}", user.getEmail(), ex);
        }
    }

    public void recordLogout(UserAuth user, String jwt, Instant issuedAt, Instant expiresAt) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(user.getEmail());
            audit.setUserAuth(user);
            audit.setRole(user.getRole().name());
            audit.setEventTime(Instant.now());
            audit.setLoginType(LoginType.LOGOUT);
            audit.setReason(LoginReason.USER_LOGOUT);
            audit.setJwtTokenHash(JwtHashUtil.hash(jwt));
            audit.setJwtIssuedAt(issuedAt);
            audit.setJwtExpiresAt(expiresAt);

            loginAuditRepository.save(audit);

            log.info("Logout audit recorded email={}", user.getEmail());

        } catch (RuntimeException ex) {
            log.error("Failed to record logout audit for email={}", user.getEmail(), ex);
        }
    }

    public void recordFailure(String email, LoginReason reason) {
        try {
            LoginAudit audit = new LoginAudit();
            audit.setEmail(email);
            audit.setEventTime(Instant.now());
            audit.setLoginType(LoginType.FAILED);
            audit.setReason(reason);

            loginAuditRepository.save(audit);

            log.warn("Login failure recorded email={} reason={}", email, reason);

        } catch (RuntimeException ex) {
            log.error("Failed to record login failure audit email={}", email, ex);
        }
    }

    public LoginAlertDTO buildLoginAlert(String email, Instant currentLoginTime) {

        log.debug("Building login alert for email={}", email);

        List<LoginAudit> logins =
                loginAuditRepository.findByEmailAndLoginTypeOrderByEventTimeAsc(
                        email,
                        LoginType.LOGIN
                );

        if (logins.size() == 1) {
            return new LoginAlertDTO(
                    LoginAlertType.FIRST_LOGIN,
                    null,
                    "Welcome! This is your first login to the system."
            );
        }

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
        if (minutes > 0 || result.isEmpty()) {
            result.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        }

        result.append("ago");

        return result.toString().trim();
    }

    public List<LoginAudit> getCurrentUserAudit() {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().isEmpty()) {
            throw new BadRequestException("Unauthenticated request");
        }

        String email = auth.getName();

        log.info("User requested own login audit email={}", email);

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

            if (success) {
                log.info("Password change audit recorded email={}", email);
            } else {
                log.warn("Password change failed email={} reason={}", email, reason);
            }

        } catch (RuntimeException ex) {
            log.error("Failed to record password change audit email={}", email, ex);
        }
    }
}
    