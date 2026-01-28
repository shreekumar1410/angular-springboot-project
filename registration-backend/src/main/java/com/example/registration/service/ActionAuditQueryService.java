package com.example.registration.service;

import com.example.registration.entity.ActionAudit;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.ActionAuditRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ActionAuditQueryService extends BaseLogger {

    private final ActionAuditRepository auditRepo;

    public ActionAuditQueryService(ActionAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public List<ActionAudit> getAll() {

        log.info("Action audit query: fetch all records");

        return auditRepo.findAll();
    }

    public List<ActionAudit> getByType(ActionType type) {

        log.info("Action audit query: fetch by type={}", type);

        return auditRepo.findByActionType(type);
    }

    public List<ActionAudit> getByStatus(ActionStatus status) {

        log.info("Action audit query: fetch by status={}", status);

        return auditRepo.findByActionStatus(status);
    }

    public List<ActionAudit> getByTypeAndStatus(ActionType type, ActionStatus status) {

        log.info(
                "Action audit query: fetch by type={} and status={}",
                type, status
        );

        return auditRepo.findByActionTypeAndActionStatus(type, status);
    }

    public List<ActionAudit> getByDateRange(Instant from, Instant to) {

        log.info(
                "Action audit query: fetch by date range from={} to={}",
                from, to
        );

        return auditRepo.findByPerformedAtBetween(from, to);
    }
}
