package com.example.registration.service;

import com.example.registration.entity.ActionAudit;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.enums.Roles;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.ActionAuditRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ActionAuditService extends BaseLogger {

    private final ActionAuditRepository auditRepo;

    public ActionAuditService(ActionAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    private String getActorEmailSafely() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated())
                ? auth.getName()
                : "SYSTEM";
    }

    private Roles getActorRoleSafely() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return Roles.SYSTEM; // or null / UNKNOWN
        }
        return Roles.valueOf(
                auth.getAuthorities().iterator().next().getAuthority()
        );
    }

    public void logAction(
            ActionType actionType,
            ActionStatus status,
            String targetEmail,
            Long targetUserId,
            String beforeState,
            String afterState,
            String reason
    ) {
        try {
            ActionAudit audit = new ActionAudit();

            audit.setActorEmail(getActorEmailSafely());
            audit.setActorRole(getActorRoleSafely());
            audit.setTargetUserEmail(targetEmail);
            audit.setTargetUserId(targetUserId);
            audit.setActionType(actionType);
            audit.setActionStatus(status);
            audit.setBeforeState(beforeState);
            audit.setAfterState(afterState);
            audit.setActionReason(reason);
            audit.setPerformedAt(Instant.now());

            auditRepo.save(audit);

            log.debug(
                    "Action audit recorded type={} status={} actor={} target={}",
                    actionType, status, audit.getActorEmail(), targetEmail
            );

        } catch (RuntimeException ex) {
            // Audit must NEVER break business flow
            log.error(
                    "Failed to record action audit type={} target={}",
                    actionType, targetEmail, ex
            );
        }
    }

    public void logFailure(
            ActionType actionType,
            String targetEmail,
            Long targetUserId,
            String reason
    ) {
        try {
            ActionAudit audit = new ActionAudit();

            audit.setActorEmail(getActorEmailSafely());
            audit.setActorRole(getActorRoleSafely());
            audit.setTargetUserEmail(targetEmail);
            audit.setTargetUserId(targetUserId);
            audit.setActionType(actionType);
            audit.setActionStatus(ActionStatus.FAILED);
            audit.setActionReason(reason);
            audit.setPerformedAt(Instant.now());

            audit.setBeforeState(null);
            audit.setAfterState(null);

            auditRepo.save(audit);

            log.warn(
                    "Action audit failure recorded type={} actor={} target={}",
                    actionType, audit.getActorEmail(), targetEmail
            );

        } catch (RuntimeException ex) {
            log.error(
                    "Failed to record FAILED action audit type={} target={}",
                    actionType, targetEmail, ex
            );
        }
    }
}
