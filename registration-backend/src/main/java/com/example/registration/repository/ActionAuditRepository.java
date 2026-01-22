package com.example.registration.repository;

import com.example.registration.entity.ActionAudit;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ActionAuditRepository extends JpaRepository<ActionAudit, Long> {
    List<ActionAudit> findByActionType(ActionType actionType);

    List<ActionAudit> findByActionStatus(ActionStatus status);

    List<ActionAudit> findByPerformedAtBetween(Instant start, Instant end);

    List<ActionAudit> findByActionTypeAndActionStatus(
            ActionType actionType,
            ActionStatus status
    );
}
