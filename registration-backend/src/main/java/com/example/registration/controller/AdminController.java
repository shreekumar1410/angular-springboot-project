package com.example.registration.controller;

import com.example.registration.constants.ApiMessages;
import com.example.registration.dto.ChangeRoleRequest;
import com.example.registration.dto.ChangeStatusRequest;
import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.entity.UserAuth;
import com.example.registration.logging.BaseLogger;
import com.example.registration.service.AdminService;
import com.example.registration.service.SupportPasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController extends BaseLogger {

    private final AdminService adminService;
    private final SupportPasswordResetService supportPasswordResetService;
    private static final String MESSAGE = "message";

    public AdminController(
            AdminService adminService,
            SupportPasswordResetService supportPasswordResetService) {

        this.adminService = adminService;
        this.supportPasswordResetService = supportPasswordResetService;
    }

    @DeleteMapping("/auth/{id}")
    public ResponseEntity<Void> deleteLoginUser(@PathVariable Long id) {

        log.info("Admin requested delete of auth user id={}", id);

        adminService.deleteLoginUser(id);

        log.info("Auth user deleted successfully id={}", id);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/auth-users")
    public List<UserAuth> getAllAuthUsers() {

        log.info("Admin requested auth user list");

        return adminService.getAllAuthUsers();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId) {

        log.info("Admin requested delete of user profile userId={}", userId);

        adminService.deleteUserAndAuth(userId);

        return ResponseEntity.ok(
                Map.of(MESSAGE, ApiMessages.USER_DELETED)
        );
    }

    @PutMapping("/change-role/{authId}")
    public ResponseEntity<Map<String, String>> changeRole(
            @PathVariable Long authId,
            @RequestBody ChangeRoleRequest request) {

        log.info("Admin requested role change for authId={} to role={}",
                authId, request.getRole());

        adminService.changeUserRole(authId, request.getRole());

        return ResponseEntity.ok(
                Map.of(MESSAGE, ApiMessages.ROLE_UPDATED)
        );
    }

    @PutMapping("/change-status/{authId}")
    public ResponseEntity<Map<String, String>> changeStatus(
            @PathVariable Long authId,
            @RequestBody ChangeStatusRequest request) {

        log.info("Admin requested status change for authId={} active={}",
                authId, request.isActive());

        adminService.changeUserStatus(authId, request.isActive());

        return ResponseEntity.ok(
                Map.of(MESSAGE, ApiMessages.STATUS_UPDATED)
        );
    }

    @GetMapping("/password-reset-audit")
    public List<PasswordResetRequest> audit() {

        log.info("Admin requested password reset audit list");

        return supportPasswordResetService.getAllRequests();
    }
}
