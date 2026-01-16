package com.example.registration.controller;

import com.example.registration.dto.ForgotPasswordRequest;
import com.example.registration.dto.ResetPasswordRequest;
import com.example.registration.service.ForgotPasswordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class ForgotPasswordController {

    private final ForgotPasswordService service;

    public ForgotPasswordController(ForgotPasswordService service) {
        this.service = service;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        String token = service.createResetToken(request);

        return ResponseEntity.ok(
                Map.of("resetToken", token)
        );
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        service.resetPassword(request);

        return ResponseEntity.ok(
                Map.of("message", "Password updated successfully")
        );
    }
}
