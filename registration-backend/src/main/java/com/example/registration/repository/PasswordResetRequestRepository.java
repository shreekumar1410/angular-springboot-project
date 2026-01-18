package com.example.registration.repository;

import com.example.registration.entity.PasswordResetRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordResetRequestRepository
        extends JpaRepository<PasswordResetRequest, Long> {

    List<PasswordResetRequest> findByStatus(String status);
}
