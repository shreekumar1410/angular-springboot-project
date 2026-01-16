package com.example.registration.dto;

public class AuthUserResponse {

    private Long id;
    private String email;
    private String role;
    private boolean profileCreated;
    private boolean active;

    public AuthUserResponse(Long id, String email, String role,
                            boolean profileCreated, boolean active) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.profileCreated = profileCreated;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isProfileCreated() {
        return profileCreated;
    }

    public boolean isActive() {
        return active;
    }
}
