package com.example.registration.dto;

public class LoginResponse {

    private LoginAlertDTO loginAlert;
    private String token;
    private String role;
    private boolean profileCreated;

    public LoginResponse(String token, String role, boolean profileCreated) {
        this.token = token;
        this.role = role;
        this.profileCreated = profileCreated;
    }

    public LoginResponse(
            String token,
            String role,
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

    public String getRole() {
        return role;
    }

    public boolean isProfileCreated() {
        return profileCreated;
    }

    public LoginAlertDTO getLoginAlert() {
        return loginAlert;
    }
}
