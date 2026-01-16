package com.example.registration.controller;

import com.example.registration.dto.ChangeRoleRequest;
import com.example.registration.dto.ChangeStatusRequest;
import com.example.registration.entity.LoginAudit;
import com.example.registration.entity.UserAuth;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.service.AdminService;
import com.example.registration.service.LoginAuditService;
import com.example.registration.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController {

    @Autowired
    private UserAuthRepository authRepo;

    private final AdminService adminService;
    private UserService userService;

    @Autowired
    private LoginAuditService loginAuditService;

    public AdminController(UserService userService, AdminService adminService) {
        this.userService = userService;
        this.adminService = adminService;
    }

    @DeleteMapping("/auth/{id}")
    public ResponseEntity<?> deleteLoginUser(@PathVariable Long id) {
        adminService.deleteLoginUser(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/auth-users")
    public List<UserAuth> getAllAuthUsers() {
        return adminService.getAllAuthUsers();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @PathVariable Long userId) {

        adminService.deleteUserAndAuth(userId);

        return ResponseEntity.ok(
                Map.of("message", "User deleted successfully")
        );
    }

    // 🔄 CHANGE ROLE
    @PutMapping("/users/{authId}/role")
    public ResponseEntity<Map<String, String>> changeRole(
            @PathVariable Long authId,
            @RequestBody ChangeRoleRequest request) {

        adminService.changeUserRole(authId, request.getRole());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Role updated successfully");

        return ResponseEntity.ok(response);
    }


    // 🔒 ACTIVATE / DEACTIVATE
    @PutMapping("/users/{authId}/status")
    public ResponseEntity<Map<String, String>> changeStatus(
            @PathVariable Long authId,
            @RequestBody ChangeStatusRequest request) {

        adminService.changeUserStatus(authId, request.isActive());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role updated successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/login-audit")
    public List<LoginAudit> getLoginAudit() {
        return loginAuditService.getAllAudits();
    }
}

