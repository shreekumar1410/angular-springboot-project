package com.example.registration.repository;

import com.example.registration.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    // 🔹 Find user profile using login email (JWT subject)
    Optional<User> findByAuthEmail(String email);

}
