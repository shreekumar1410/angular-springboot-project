package com.example.registration.service;

import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.enums.PasswordResetStatus;
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
    ActionAuditService actionAuditService;



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

        System.out.println("this is from supportservice.");
        System.out.println("Email received = [" + email + "]");
        System.out.println("Email length = " + email.length());

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PasswordResetRequest req = new PasswordResetRequest();
        req.setUserAuth(auth);
        req.setUserEmail(email);
        req.setStatus(PasswordResetStatus.REQUESTED);
        req.setRequestedAt(LocalDateTime.now());

        requestRepo.save(req);
    }

    // ================= SUPPORT / ADMIN / SUPER_ADMIN: VIEW =================

    public List<PasswordResetRequest> getAllRequests() {

        return requestRepo.findAll();
    }

    // ================= SUPPORT: APPROVE & RESET =================

//    public void approveAndReset(Long requestId) {
//
//        PasswordResetRequest req = requestRepo.findById(requestId)
//                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
//
//        UserAuth auth = req.getUserAuth();
//
//        // generate random password
//        String generatedPassword = UUID.randomUUID()
//                .toString()
//                .substring(0, 10);
//
//        // update auth password
//        auth.setPassword(passwordEncoder.encode(generatedPassword));
//        authRepo.save(auth);
//
//        // audit update
//        String supportEmail = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName();
//
//        req.setStatus("PASSWORD_SENT");
//        req.setApprovedBy(supportEmail);
//        req.setApprovedAt(LocalDateTime.now());
//        req.setPasswordSentAt(LocalDateTime.now());
//
//        requestRepo.save(req);
//
//        // simulate sending password
//        System.out.println(
//                "TEMP PASSWORD for " + auth.getEmail() + " = " + generatedPassword
//        );
//    }

    public void acceptRequest(Long requestId) {

        PasswordResetRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (req.getStatus() != PasswordResetStatus.REQUESTED) {
            throw new IllegalStateException("Request already processed");
        }

        // generate random password
        String generatedPassword = UUID.randomUUID()
                .toString()
                .substring(0, 10);

        req.setTempPasswordPlain(generatedPassword);

        // store HASH ONLY (not plain)
        req.setTempPasswordHash(
                passwordEncoder.encode(generatedPassword)
        );

        String supportEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        req.setStatus(PasswordResetStatus.ACCEPTED);
        req.setApprovedBy(supportEmail);
        req.setApprovedAt(LocalDateTime.now());

        requestRepo.save(req);

        // log only for SUPPORT reference (optional)
        System.out.println(
                "Password generated for " + req.getUserEmail()
                        + " (NOT SENT YET)"
        );
    }

    public void sendPassword(Long requestId) {

        PasswordResetRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (req.getStatus() != PasswordResetStatus.ACCEPTED) {
            throw new IllegalStateException("Request not accepted yet");
        }

        UserAuth auth = req.getUserAuth();

        // update actual password (HASH)
        auth.setPassword(req.getTempPasswordHash());
        authRepo.save(auth);

        // simulate sending password (PLAIN)
        System.out.println(
                "TEMP PASSWORD SENT to " + auth.getEmail()
                        + " . TEMP PASSWORD = " + req.getTempPasswordPlain()
        );

        // cleanup plain password
        req.setTempPasswordPlain(null);

        req.setStatus(PasswordResetStatus.PASSWORD_SENT);
        req.setPasswordSentAt(LocalDateTime.now());

        actionAuditService.logAction(
                ActionType.PASSWORD_RESET,
                ActionStatus.SUCCESS,
                auth.getEmail(),
                auth.getId(),
                "OLD_PASSWORD",
                "PASSWORD_RESET",
                "Support reset password"
        );


        requestRepo.save(req);
    }



}
