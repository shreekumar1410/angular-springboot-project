package com.example.registration.service;

import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.entity.UserAuth;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.repository.PasswordResetRequestRepository;
import com.example.registration.repository.UserAuthRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class SupportPasswordResetService {

    private final PasswordResetRequestRepository requestRepo;
    private final UserAuthRepository authRepo;
    private final PasswordEncoder passwordEncoder;

    public SupportPasswordResetService(
            PasswordResetRequestRepository requestRepo,
            UserAuthRepository authRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.requestRepo = requestRepo;
        this.authRepo = authRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= USER: RAISE REQUEST =================

    public void raiseRequest(String email) {

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PasswordResetRequest req = new PasswordResetRequest();
        req.setUserAuth(auth);
        req.setUserEmail(email);
        req.setStatus("REQUESTED");
        req.setRequestedAt(LocalDateTime.now());

        requestRepo.save(req);
    }

    // ================= SUPPORT / ADMIN / SUPER_ADMIN: VIEW =================

    public List<PasswordResetRequest> getAllRequests() {
        return requestRepo.findAll();
    }

    // ================= SUPPORT: APPROVE & RESET =================

    public void approveAndReset(Long requestId) {

        PasswordResetRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        UserAuth auth = req.getUserAuth();

        // generate random password
        String generatedPassword = UUID.randomUUID()
                .toString()
                .substring(0, 10);

        // update auth password
        auth.setPassword(passwordEncoder.encode(generatedPassword));
        authRepo.save(auth);

        // audit update
        String supportEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        req.setStatus("PASSWORD_SENT");
        req.setApprovedBy(supportEmail);
        req.setApprovedAt(LocalDateTime.now());
        req.setPasswordSentAt(LocalDateTime.now());

        requestRepo.save(req);

        // simulate sending password
        System.out.println(
                "TEMP PASSWORD for " + auth.getEmail() + " = " + generatedPassword
        );
    }
}
