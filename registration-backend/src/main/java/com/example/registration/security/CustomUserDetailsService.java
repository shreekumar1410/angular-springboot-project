package com.example.registration.security;

import com.example.registration.entity.UserAuth;
import com.example.registration.repository.UserAuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserAuthRepository authRepo;

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return User.builder()
                .username(auth.getEmail())
                .password(auth.getPassword())
                .roles(auth.getRole())
                .build();
    }
}

