package com.example.registration.controller;

import com.example.registration.entity.ActionAudit;
import com.example.registration.repository.ActionAuditRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/super-admin/action-audit")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class ActionAuditController {

    private final ActionAuditRepository auditRepo;

    public ActionAuditController(ActionAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    @GetMapping
    public List<ActionAudit> getAllAudits() {
        return auditRepo.findAll();
    }
}
