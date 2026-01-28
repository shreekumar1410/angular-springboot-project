package com.example.registration.dto;

public class UserShortResponse implements UserViewResponse {

    private Long sno;
    private String name;
    private String email;
    private String phone;

    public UserShortResponse(Long sno, String name, String email, String phone) {
        this.sno = sno;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public Long getSno() {
        return sno;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
