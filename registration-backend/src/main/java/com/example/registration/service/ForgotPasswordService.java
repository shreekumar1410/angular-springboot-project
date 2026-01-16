package com.example.registration.service;

import com.example.registration.dto.ForgotPasswordRequest;
import com.example.registration.dto.ResetPasswordRequest;
import com.example.registration.entity.PasswordResetToken;
import com.example.registration.entity.UserAuth;
import com.example.registration.exception.BadRequestException;
import com.example.registration.repository.PasswordResetTokenRepository;
import com.example.registration.repository.UserAuthRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ForgotPasswordService {

    private final UserAuthRepository authRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordService(
            UserAuthRepository authRepo,
            PasswordResetTokenRepository tokenRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.authRepo = authRepo;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // STEP 1: VERIFY EMAIL & CREATE TOKEN
    public String createResetToken(ForgotPasswordRequest request) {

        UserAuth auth = authRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // token valid 10 min
        resetToken.setUserAuth(auth);
        resetToken.setEmail(auth.getEmail());
        resetToken.setUsed(false);

        tokenRepo.save(resetToken);

        return token;
    }

    // STEP 2: RESET PASSWORD
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = tokenRepo.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new BadRequestException("Token already used");
        }

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token expired");
        }

        UserAuth auth = resetToken.getUserAuth();
        auth.setPassword(passwordEncoder.encode(request.getNewPassword()));
        authRepo.save(auth);

        resetToken.setUsed(true);
        tokenRepo.save(resetToken);
    }
}
