package com.example.registration.service;

import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.enums.PasswordResetStatus;
import com.example.registration.exception.BadRequestException;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.PasswordResetRequestRepository;
import com.example.registration.repository.UserAuthRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SupportPasswordResetService extends BaseLogger {

    private final PasswordResetRequestRepository requestRepo;
    private final UserAuthRepository authRepo;
    private final PasswordEncoder passwordEncoder;
    private final ActionAuditService actionAuditService;

    public SupportPasswordResetService(
            PasswordResetRequestRepository requestRepo,
            UserAuthRepository authRepo,
            PasswordEncoder passwordEncoder,
            ActionAuditService actionAuditService
    ) {
        this.requestRepo = requestRepo;
        this.authRepo = authRepo;
        this.passwordEncoder = passwordEncoder;
        this.actionAuditService = actionAuditService;
    }

    // ================= USER: RAISE REQUEST =================

    public void raiseRequest(String email) {

        log.info("Password reset request raised for email={}", email);

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PasswordResetRequest req = new PasswordResetRequest();
        req.setUserAuth(auth);
        req.setUserEmail(email);
        req.setStatus(PasswordResetStatus.REQUESTED);
        req.setRequestedAt(LocalDateTime.now());

        requestRepo.save(req);

        log.info("Password reset request stored for email={}", email);
    }

    // ================= SUPPORT / ADMIN / SUPER_ADMIN: VIEW =================

    public List<PasswordResetRequest> getAllRequests() {

        log.info("Password reset request list requested");

        return requestRepo.findAll();
    }

    // ================= SUPPORT: ACCEPT REQUEST =================

    public void acceptRequest(Long requestId) {

        log.info("Password reset accept attempt requestId={}", requestId);

        PasswordResetRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (req.getStatus() != PasswordResetStatus.REQUESTED) {
            log.warn("Password reset accept denied - invalid state requestId={}", requestId);
            throw new IllegalStateException("Request already processed");
        }

        String generatedPassword = UUID.randomUUID()
                .toString()
                .substring(0, 10);

        req.setTempPasswordPlain(generatedPassword);
        req.setTempPasswordHash(passwordEncoder.encode(generatedPassword));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().isEmpty()) {
            throw new BadRequestException("Unauthenticated request");
        }

        String supportEmail = auth.getName();

        req.setStatus(PasswordResetStatus.ACCEPTED);
        req.setApprovedBy(supportEmail);
        req.setApprovedAt(LocalDateTime.now());

        requestRepo.save(req);

        log.info("Password reset request accepted requestId={} approvedBy={}",
                requestId, supportEmail);
    }

    // ================= SUPPORT: SEND PASSWORD =================

    public void sendPassword(Long requestId) {

        PasswordResetRequest req = null;

        try {
            log.info("Password reset send attempt requestId={}", requestId);

            req = requestRepo.findById(requestId)
                    .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

            if (req.getStatus() != PasswordResetStatus.ACCEPTED) {
                log.warn("Password reset send denied - request not accepted requestId={}", requestId);
                throw new IllegalStateException("Request not accepted yet");
            }

            UserAuth auth = req.getUserAuth();

            auth.setPassword(req.getTempPasswordHash());
            authRepo.save(auth);

            String authUser = auth.getEmail();

            log.info("");
            log.info("User {} Password for = {}",authUser, req.getTempPasswordPlain());
            log.info("");

            req.setTempPasswordPlain(null);
            req.setStatus(PasswordResetStatus.PASSWORD_SENT);
            req.setPasswordSentAt(LocalDateTime.now());

            actionAuditService.logAction(
                    ActionType.PASSWORD_RESET,
                    ActionStatus.SUCCESS,
                    auth.getEmail(),
                    auth.getId(),
                    "OLD_PASSWORD",
                    "PASSWORD_RESET",
                    "Support reset password"
            );

            requestRepo.save(req);

            log.info("Password reset completed successfully for email={}", auth.getEmail());

        } catch (RuntimeException ex) {

            actionAuditService.logFailure(
                    ActionType.PASSWORD_RESET,
                    req != null ? req.getUserAuth().getEmail() : null,
                    req != null ? req.getUserAuth().getId() : null,
                    ex.getMessage()
            );

            log.error("Password reset failed requestId={}", requestId, ex);
        }
    }
}
