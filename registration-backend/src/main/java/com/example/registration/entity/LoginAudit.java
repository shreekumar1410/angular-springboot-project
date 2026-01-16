package com.example.registration.entity;

import com.example.registration.enums.LoginReason;
import com.example.registration.enums.LoginType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "login_audit")
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_auth_id")
    private UserAuth userAuth;

    private String role;

    private Instant eventTime;

    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    private String jwtTokenHash;

    private Instant jwtIssuedAt;

    private Instant jwtExpiresAt;

    @Enumerated(EnumType.STRING)
    private LoginReason loginReason;

    /* getters & setters */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserAuth getUserAuth() { return userAuth; }
    public void setUserAuth(UserAuth userAuth) { this.userAuth = userAuth; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }

    public LoginType getLoginType() { return loginType; }
    public void setLoginType(LoginType loginType) { this.loginType = loginType; }

    public String getJwtTokenHash() { return jwtTokenHash; }
    public void setJwtTokenHash(String jwtTokenHash) { this.jwtTokenHash = jwtTokenHash; }

    public Instant getJwtIssuedAt() { return jwtIssuedAt; }
    public void setJwtIssuedAt(Instant jwtIssuedAt) { this.jwtIssuedAt = jwtIssuedAt; }

    public Instant getJwtExpiresAt() { return jwtExpiresAt; }
    public void setJwtExpiresAt(Instant jwtExpiresAt) { this.jwtExpiresAt = jwtExpiresAt; }

    public LoginReason getReason() { return loginReason; }
    public void setReason(LoginReason loginReason) { this.loginReason = loginReason; }
}
