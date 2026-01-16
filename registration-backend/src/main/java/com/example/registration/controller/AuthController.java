package com.example.registration.controller;

import com.example.registration.config.JwtUtil;
import com.example.registration.dto.LoginRequest;
import com.example.registration.dto.LoginResponse;
import com.example.registration.dto.RegisterRequest;
import com.example.registration.service.AuthService;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.service.LoginAuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private final AuthService authService;
    private final UserAuthRepository userAuthRepository;
    private final LoginAuditService loginAuditService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, UserAuthRepository userAuthRepository, LoginAuditService loginAuditService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.userAuthRepository = userAuthRepository;
        this.loginAuditService = loginAuditService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
//        System.out.println("response = " + response);
        return ResponseEntity.ok(response);
    }
//    @PostMapping("/register")
//    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
//        authService.register(request);
//        return ResponseEntity.ok("User registered successfully");
        //    }
        @PostMapping("/register")
        public ResponseEntity<Map<String, String>> register(
                @RequestBody RegisterRequest request) {

            authService.register(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Registration successful");

            return ResponseEntity.ok(response);
        }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return ResponseEntity.ok().build();
        }

        String jwt = header.substring(7);
        String email = jwtUtil.extractEmail(jwt);


        userAuthRepository.findByEmail(email).ifPresent(user ->
                loginAuditService.recordLogout(
                        user,
                        jwt,
                        jwtUtil.extractIssuedAt(jwt).toInstant(),
                        jwtUtil.extractExpiration(jwt).toInstant()
                )
        );

        return ResponseEntity.ok().build();
    }


}
