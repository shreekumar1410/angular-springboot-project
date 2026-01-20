package com.example.registration.dto;

import com.example.registration.enums.Roles;

public class ChangeRoleRequest {

    private Roles role; // ADMIN or USER

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }
}
