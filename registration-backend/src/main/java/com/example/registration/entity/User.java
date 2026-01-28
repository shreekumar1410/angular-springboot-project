package com.example.registration.entity;

import com.example.registration.dto.UserProfileRequest;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    public static User fromProfileRequest(
            UserProfileRequest request,
            UserAuth auth
    ) {
        User user = new User();

        user.setName(request.getName());
        user.setAge(request.getAge());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setQualification(request.getQualification());
        user.setDob(request.getDob());
        user.setLanguages(request.getLanguages());

        user.setEmailId(auth.getEmail());
        user.setAuth(auth);

        return user;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String emailId;
    private Integer age;
    private String address;
    private String phone;
    private String gender;
    private String qualification;
    private LocalDate dob;
    private String languages;

    @ManyToOne
    @JoinColumn(name = "auth_id")
    private UserAuth auth;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public Integer  getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public UserAuth getAuth() {
        return auth;
    }

    public void setAuth(UserAuth auth) {
        this.auth = auth;
    }



}
