package com.example.registration.dto;

import com.example.registration.enums.LoginAlertType;
import java.time.Instant;

public class LoginAlertDTO {

    private LoginAlertType type;
    private Instant lastLogoutTime;
    private String message;

    public LoginAlertDTO(
            LoginAlertType type,
            Instant lastLogoutTime,
            String message
    ) {
        this.type = type;
        this.lastLogoutTime = lastLogoutTime;
        this.message = message;
    }

    public LoginAlertType getType() {
        return type;
    }

    public Instant getLastLogoutTime() {
        return lastLogoutTime;
    }

    public String getMessage() {
        return message;
    }
}
