package com.example.registration.dto;

public class UserFullResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String dob;
    private String languages;

    public UserFullResponse(Long id, String name, String email,
                            String phone, String address,
                            String dob, String languages) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
        this.languages = languages;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getDob() { return dob; }
    public String getLanguages() { return languages; }
}
