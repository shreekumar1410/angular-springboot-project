package com.example.registration.service;

import com.example.registration.entity.ActionAudit;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.repository.ActionAuditRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ActionAuditQueryService {

    private final ActionAuditRepository auditRepo;

    public ActionAuditQueryService(ActionAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public List<ActionAudit> getAll() {
        return auditRepo.findAll();
    }

    public List<ActionAudit> getByType(ActionType type) {
        return auditRepo.findByActionType(type);
    }

    public List<ActionAudit> getByStatus(ActionStatus status) {
        return auditRepo.findByActionStatus(status);
    }

    public List<ActionAudit> getByTypeAndStatus(ActionType type, ActionStatus status) {
        return auditRepo.findByActionTypeAndActionStatus(type, status);
    }

    public List<ActionAudit> getByDateRange(Instant from, Instant to) {
        return auditRepo.findByPerformedAtBetween(from, to);
    }
}
