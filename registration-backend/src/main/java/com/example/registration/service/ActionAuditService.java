package com.example.registration.service;

import com.example.registration.entity.ActionAudit;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.enums.Roles;
import com.example.registration.repository.ActionAuditRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ActionAuditService {

    private final ActionAuditRepository auditRepo;

    public ActionAuditService(ActionAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
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

        ActionAudit audit = new ActionAudit();

        String actorEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Roles actorRole = Roles.valueOf(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
        );

        audit.setActorEmail(actorEmail);
        audit.setActorRole(actorRole);
        audit.setTargetUserEmail(targetEmail);
        audit.setTargetUserId(targetUserId);
        audit.setActionType(actionType);
        audit.setActionStatus(status);
        audit.setBeforeState(beforeState);
        audit.setAfterState(afterState);
        audit.setActionReason(reason);
        audit.setPerformedAt(Instant.now());

        auditRepo.save(audit);
    }
}
