package com.example.registration.entity;

import com.example.registration.enums.PasswordResetStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_request")
public class PasswordResetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_auth_id", nullable = false)
    private UserAuth userAuth;

    private String userEmail;

    private String tempPasswordPlain;

    private String tempPasswordHash;

    @Enumerated(EnumType.STRING)
    private PasswordResetStatus status;// REQUESTED, APPROVED, PASSWORD_SENT, CLOSED

    private String approvedBy; // SUPPORT email
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime passwordSentAt;

    private String remarks;

    // getters & setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserAuth getUserAuth() {
        return userAuth;
    }

    public void setUserAuth(UserAuth userAuth) {
        this.userAuth = userAuth;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getTempPasswordPlain() {
        return tempPasswordPlain;
    }

    public void setTempPasswordPlain(String tempPasswordPlain) {
        this.tempPasswordPlain = tempPasswordPlain;
    }

    public String getTempPasswordHash() {
        return tempPasswordHash;
    }

    public void setTempPasswordHash(String tempPasswordHash) {
        this.tempPasswordHash = tempPasswordHash;
    }

    public PasswordResetStatus getStatus() {
        return status;
    }

    public void setStatus(PasswordResetStatus status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getPasswordSentAt() {
        return passwordSentAt;
    }

    public void setPasswordSentAt(LocalDateTime passwordSentAt) {
        this.passwordSentAt = passwordSentAt;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
