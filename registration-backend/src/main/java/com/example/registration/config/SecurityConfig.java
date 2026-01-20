package com.example.registration.config;

import com.example.registration.enums.Roles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;


@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ✅ FIXED CORS CONFIG (Spring Security 6+)
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(auth -> auth
                        // ✅ ALLOW PREFLIGHT REQUESTS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // PUBLIC ENDPOINTS
                        .requestMatchers("/auth/**")
                            .permitAll()

                        // USER + ADMIN + SUPER ADMIN
                        .requestMatchers("/user/**")
                            .hasAnyAuthority(Roles.USER.name(), Roles.EDITOR.name(), Roles.ADMIN.name(), Roles.SUPER_ADMIN.name())

                        .requestMatchers("/support/**")
                            .hasAnyAuthority(Roles.SUPPORT.name(), Roles.SUPER_ADMIN.name())

                        // ADMIN + SUPER ADMIN
                        .requestMatchers("/admin/**")
                        .hasAnyAuthority(Roles.ADMIN.name(), Roles.SUPER_ADMIN.name())

                        // SUPER ADMIN
                        .requestMatchers("/super-admin/**")
                            .hasAuthority(Roles.SUPER_ADMIN.name())

                        // EVERYTHING ELSE NEEDS AUTH
                        .anyRequest().authenticated()
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

