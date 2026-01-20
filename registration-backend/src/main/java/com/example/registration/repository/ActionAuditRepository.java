package com.example.registration.repository;

import com.example.registration.entity.ActionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionAuditRepository extends JpaRepository<ActionAudit, Long> {
}
