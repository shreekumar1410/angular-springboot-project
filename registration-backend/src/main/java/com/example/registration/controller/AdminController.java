package com.example.registration.controller;

import com.example.registration.dto.ChangeRoleRequest;
import com.example.registration.dto.ChangeStatusRequest;
import com.example.registration.entity.LoginAudit;
import com.example.registration.entity.PasswordResetRequest;
import com.example.registration.entity.UserAuth;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.service.AdminService;
import com.example.registration.service.LoginAuditService;
import com.example.registration.service.SupportPasswordResetService;
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

//    @Autowired
    private UserAuthRepository authRepo;

    private final AdminService adminService;
    private final SupportPasswordResetService supportPasswordResetService;
    private UserService userService;

    @Autowired
    private LoginAuditService loginAuditService;

    public AdminController( AdminService adminService, SupportPasswordResetService supportPasswordResetService) {
        this.adminService = adminService;
        this.supportPasswordResetService = supportPasswordResetService;
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
    @PutMapping("/change-role/{authId}")
    public ResponseEntity<Map<String, String>> changeRole(
            @PathVariable Long authId,
            @RequestBody ChangeRoleRequest request) {

        adminService.changeUserRole(authId, request.getRole());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Role updated successfully");

        return ResponseEntity.ok(response);
    }


    // 🔒 ACTIVATE / DEACTIVATE
    @PutMapping("/change-status/{authId}")
    public ResponseEntity<Map<String, String>> changeStatus(
            @PathVariable Long authId,
            @RequestBody ChangeStatusRequest request) {

        adminService.changeUserStatus(authId, request.isActive());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Role updated successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/password-reset-audit")
    public List<PasswordResetRequest> audit() {

        return supportPasswordResetService.getAllRequests();
    }

}

