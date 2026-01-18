package com.example.registration.controller;

import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.service.SupportPasswordResetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/support/password-reset")
public class SupportPasswordController {

    private final SupportPasswordResetService service;

    public SupportPasswordController(SupportPasswordResetService service) {
        this.service = service;
    }

    @GetMapping("/requests")
    public List<PasswordResetRequest> getAll() {
        return service.getAllRequests();
    }

    @PostMapping("/approve/{id}")
    public void approve(@PathVariable Long id) {
        service.approveAndReset(id);
    }
}
