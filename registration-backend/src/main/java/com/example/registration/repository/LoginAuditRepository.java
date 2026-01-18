package com.example.registration.repository;

import com.example.registration.entity.LoginAudit;
import com.example.registration.enums.LoginType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {

    List<LoginAudit> findByEmailAndLoginTypeOrderByEventTimeAsc(
            String email,
            LoginType loginType
    );

    Optional<LoginAudit> findTopByEmailAndLoginTypeOrderByEventTimeDesc(
            String email,
            LoginType loginType
    );

        List<LoginAudit> findByEmail(String email);
}
