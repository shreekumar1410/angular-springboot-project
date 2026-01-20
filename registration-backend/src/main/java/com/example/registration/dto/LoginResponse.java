package com.example.registration.dto;

import com.example.registration.enums.Roles;

public class LoginResponse {

    private LoginAlertDTO loginAlert;
    private String token;
    private Roles role;
    private boolean profileCreated;

    public LoginResponse(String token, Roles role, boolean profileCreated) {
        this.token = token;
        this.role = role;
        this.profileCreated = profileCreated;
    }

    public LoginResponse(
            String token,
            Roles role,
            boolean profileCreated,
            LoginAlertDTO loginAlert
    ) {
        this.token = token;
        this.role = role;
        this.profileCreated = profileCreated;
        this.loginAlert = loginAlert;
    }


    public String getToken() {
        return token;
    }

    public Roles getRole() {
        return role;
    }

    public boolean isProfileCreated() {
        return profileCreated;
    }

    public LoginAlertDTO getLoginAlert() {
        return loginAlert;
    }
}
