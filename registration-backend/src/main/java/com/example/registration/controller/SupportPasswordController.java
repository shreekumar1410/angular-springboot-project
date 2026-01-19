package com.example.registration.controller;

import com.example.registration.entity.LoginAudit;
import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.service.LoginAuditService;
import com.example.registration.service.SupportPasswordResetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/support")
public class SupportPasswordController {

    private final LoginAuditService loginAuditService;
    private final SupportPasswordResetService supportPasswordResetService;

    public SupportPasswordController(LoginAuditService loginAuditService, SupportPasswordResetService supportPasswordResetService) {
        this.loginAuditService = loginAuditService;
        this.supportPasswordResetService = supportPasswordResetService;
    }

    @GetMapping("/login-audit")
    public List<LoginAudit> getLoginAudit() {
        return loginAuditService.getAllAudits();
    }

    //FORGET PASSWORD

//    @GetMapping("/password-reset-audit")
//    public List<PasswordResetRequest> audit() {
//
//        return supportPasswordResetService.getAllRequests();
//    }

    @GetMapping("/password-reset/requests")
    public List<PasswordResetRequest> getAll() {
        return supportPasswordResetService.getAllRequests();
    }

    @PostMapping("/password-reset/accept/{requestId}")
    public void acceptRequest(@PathVariable Long requestId) {
        supportPasswordResetService.acceptRequest(requestId);
    }

    @PostMapping("/password-reset/send/{requestId}")
    public void sendPassword(@PathVariable Long requestId) {
        supportPasswordResetService.sendPassword(requestId);
    }
}
