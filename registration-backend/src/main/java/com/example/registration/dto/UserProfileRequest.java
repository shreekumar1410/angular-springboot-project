package com.example.registration.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class UserProfileRequest {

    @NotBlank(message = "Name is required")
//    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^d{9}$", message = "Invalid phone number")
    private String phone;

    @NotBlank(message = "Address is required")
//    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    private String address;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

//    @NotBlank(message = "Gender is required")
    private String gender;

//    @NotBlank(message = "Qualification is required")
    private String qualification;

//    @NotEmpty(message = "Languages are required")
    private String languages;

//    @NotNull(message = "Age is required")
//    @Min(value = 18, message = "Age must be at least 18")
//    @Max(value = 100, message = "Age must be less than 100")
    private Integer age;

    private String emailId;

    // getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
