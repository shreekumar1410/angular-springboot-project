package com.example.registration.security;

import com.example.registration.entity.UserAuth;
import com.example.registration.repository.UserAuthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log =
            LoggerFactory.getLogger(CustomUserDetailsService.class);

    private UserAuthRepository authRepo;

    @Override
    public UserDetails loadUserByUsername(String email) {

        log.debug("Loading user details for authentication");

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed - user not found");
                    return new UsernameNotFoundException("User not found");
                });

        log.debug("User details loaded successfully");

        return User.builder()
                .username(auth.getEmail())
                .password(auth.getPassword())
                .authorities(auth.getRole().name())
                .build();
    }
}
