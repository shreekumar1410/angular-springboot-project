package com.example.registration.controller;

import com.example.registration.dto.UserProfileRequest;
import com.example.registration.dto.UserViewResponse;
import com.example.registration.entity.LoginAudit;
import com.example.registration.entity.User;
import com.example.registration.entity.UserAuth;
import com.example.registration.exception.AccessDeniedException;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin("*")
public class UserController extends BaseLogger {

    private final UserService userService;
    private final UserAuthRepository authRepo;
    private final LoginAuditService loginAuditService;
    private final AdminService adminService;

    public UserController(
            UserService userService,
            UserAuthRepository authRepo,
            LoginAuditService loginAuditService,
            AdminService adminService) {

        this.userService = userService;
        this.authRepo = authRepo;
        this.loginAuditService = loginAuditService;
        this.adminService = adminService;
    }

    /**
     * CREATE OWN PROFILE (FIRST TIME)
     */
    @PostMapping("/profile")
    public User createProfile(@Valid @RequestBody UserProfileRequest request) {

        log.info("User requested to create own profile");


        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Profile creation failed - unauthenticated access");
            throw new AccessDeniedException("Unauthorized");
        }

        String email = authentication.getName();

        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Unauthorized"));

        if (auth.isProfileCreated()) {
            log.warn("Profile creation blocked - already created for authId={}", auth.getId());
            throw new AccessDeniedException("Profile already created");
        }

        User user = userService.createProfile(auth.getId(), request);

        log.info("Profile created successfully for authId={}", auth.getId());

        return user;
    }

    /**
     * CREATE PROFILE BY EDITOR
     */
    @PostMapping("/profile/{authId}")
    public User createProfileForUser(
            @PathVariable Long authId,
            @Valid @RequestBody UserProfileRequest request) {

        log.info("Editor requested profile creation for authId={}", authId);

        User user = userService.createProfileByEditor(authId, request);

        log.info("Profile created by editor for authId={}", authId);

        return user;
    }

    /**
     * VIEW USERS (ROLE BASED)
     */
    @GetMapping
    public List<UserViewResponse> getUser() {

        log.info("User requested user list (role-based)");

        return userService.getUsersByRole();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {

        log.info("User requested profile details for userId={}", id);

        return userService.getUserById(id);
    }

    /**
     * UPDATE OWN PROFILE
     */
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @Valid @RequestBody User user) {

        log.info("User requested profile update for userId={}", id);

        User updatedUser = userService.updateUser(id, user);

        log.info("Profile updated successfully for userId={}", id);

        return updatedUser;
    }

    @GetMapping("/me")
    public User getMyProfile() {

        log.info("User requested own profile");

        return userService.getMyProfile();
    }

    @GetMapping("/me/login-history")
    public List<LoginAudit> getMyLoginHistory() {

        log.info("User requested own login history");

        return loginAuditService.getCurrentUserAudit();
    }

    @GetMapping("/auth-users")
    public List<UserAuth> getAllAuthUsers() {

        log.info("Admin requested auth users list via user controller");

        return adminService.getAllAuthUsers();
    }
}
