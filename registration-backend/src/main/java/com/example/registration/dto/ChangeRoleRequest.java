package com.example.registration.dto;

public class ChangeRoleRequest {

    private String role; // ADMIN or USER

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
