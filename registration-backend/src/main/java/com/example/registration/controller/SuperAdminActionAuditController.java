package com.example.registration.controller;

import com.example.registration.entity.ActionAudit;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.logging.BaseLogger;
import com.example.registration.service.ActionAuditQueryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/super-admin/action-audit")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class SuperAdminActionAuditController extends BaseLogger {

    private final ActionAuditQueryService queryService;

    public SuperAdminActionAuditController(ActionAuditQueryService queryService) {
        this.queryService = queryService;
    }

    // 🔹 1. Get ALL audits
    @GetMapping
    public List<ActionAudit> getAllAudits() {

        log.info("SUPER_ADMIN requested all action audits");

        return queryService.getAll();
    }

    // 🔹 2. Filter by action type
    @GetMapping("/type/{type}")
    public List<ActionAudit> getByType(@PathVariable ActionType type) {

        log.info("SUPER_ADMIN requested audits by type={}", type);

        return queryService.getByType(type);
    }

    // 🔹 3. Filter by status (SUCCESS / FAILED)
    @GetMapping("/status/{status}")
    public List<ActionAudit> getByStatus(@PathVariable ActionStatus status) {

        log.info("SUPER_ADMIN requested audits by status={}", status);

        return queryService.getByStatus(status);
    }

    // 🔹 4. Filter by type + status
    @GetMapping("/filter")
    public List<ActionAudit> getByTypeAndStatus(
            @RequestParam ActionType type,
            @RequestParam ActionStatus status
    ) {

        log.info(
                "SUPER_ADMIN requested audits by type={} and status={}",
                type, status
        );

        return queryService.getByTypeAndStatus(type, status);
    }

    // 🔹 5. Filter by date range
    @GetMapping("/date-range")
    public List<ActionAudit> getByDateRange(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {

        log.info(
                "SUPER_ADMIN requested audits from={} to={}",
                from, to
        );

        return queryService.getByDateRange(from, to);
    }
}
