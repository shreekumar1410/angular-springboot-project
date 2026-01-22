package com.example.registration.dto;

import com.example.registration.enums.Roles;
import org.springframework.validation.annotation.Validated;

public class RegisterRequest {
    
    private String email;
    private String password;
    private Roles role; // optional (ADMIN / USER)

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }
}
