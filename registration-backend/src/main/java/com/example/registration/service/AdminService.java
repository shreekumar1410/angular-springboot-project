package com.example.registration.service;

import com.example.registration.entity.User;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.exception.AccessDeniedException;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.repository.UserRepository;
import com.example.registration.enums.Roles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final UserAuthRepository authRepo;
    private final UserRepository userRepo;
    private final ActionAuditService actionAuditService;

    public AdminService(UserRepository userRepo, UserAuthRepository authRepo, ActionAuditService actionAuditService) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.actionAuditService = actionAuditService;
    }

    // =====================================================
    // 🔐 COMMON HELPERS
    // =====================================================

    private Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String getLoggedInEmail() {
        return getAuth().getName();
    }

    private String getLoggedInRole() {
        return getAuth().getAuthorities()
                .iterator()
                .next()
                .getAuthority();
    }

    private boolean isAdmin() {
        return Roles.ADMIN.equals(getLoggedInRole());
    }

    private boolean isSuperAdmin() {
        return Roles.SUPER_ADMIN.equals(getLoggedInRole());
    }

    // =====================================================
    // 🔍 VIEW AUTH USERS
    // =====================================================

    public List<UserAuth> getAllAuthUsers() {
        return authRepo.findAll();
    }

    public void deleteLoginUser(Long authId) {
        if (!authRepo.existsById(authId)) {
            throw new ResourceNotFoundException("User not found");
        }
        authRepo.deleteById(authId);
    }

    // =====================================================
    // 🔐 CHANGE USER ROLE
    // =====================================================

    public void changeUserRole(Long authId, Roles newRole) {

        UserAuth target = null;
        try {
            target = authRepo.findById(authId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // ❌ Prevent self role change
            if (target.getEmail().equals(getLoggedInEmail())) {
                throw new AccessDeniedException("You cannot change your own role");
            }

            // ADMIN RULES
            if (isAdmin()) {

                // ADMIN cannot modify ADMIN or SUPER_ADMIN
                if (!Roles.USER.equals(target.getRole())
                        && !Roles.SUPPORT.equals(target.getRole())) {
                    throw new AccessDeniedException("ADMIN cannot modify admin roles");
                }

                // ADMIN can only assign USER or SUPPORT
                if (!Roles.USER.equals(newRole) && !Roles.SUPPORT.equals(newRole)) {
                    throw new AccessDeniedException("ADMIN can assign only USER or SUPPORT");
                }
            }

            // SUPER_ADMIN RULES
            if (isSuperAdmin()) {

                // SUPER_ADMIN can assign USER / SUPPORT / ADMIN
                if (!Roles.USER.equals(newRole)
                        && !Roles.SUPPORT.equals(newRole)
                        && !Roles.ADMIN.equals(newRole)
                        && !Roles.EDITOR.equals(newRole)){
                    throw new RuntimeException("Invalid role");
                }
            }
            //for action aduit
            String before = target.getRole().name();

            target.setRole(newRole);
            authRepo.save(target);

            //for action aduit
            actionAuditService.logAction(
                    ActionType.ROLE_CHANGE,
                    ActionStatus.SUCCESS,
                    target.getEmail(),
                    target.getId(),
                    before,
                    newRole.name(),
                    "Role updated"
            );
        } catch (RuntimeException ex) {
            actionAuditService.logFailure(
                    ActionType.ROLE_CHANGE,
                    target != null ? target.getEmail() : null,
                    target != null ? target.getId() : null,
                    ex.getMessage()
            );

            throw ex;
        }

    }

    // =====================================================
    // 🔐 ACTIVATE / DEACTIVATE LOGIN
    // =====================================================

//    public void changeUserStatus(Long authId, boolean active) {
//
//        UserAuth target = authRepo.findById(authId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        // ❌ Prevent self deactivation
//        if (target.getEmail().equals(getLoggedInEmail())) {
//            throw new AccessDeniedException("You cannot change your own account status");
//        }
//
//        // ADMIN cannot deactivate ADMIN or SUPER_ADMIN
//        if (isAdmin() &&
//                (Roles.ADMIN.equals(target.getRole())
//                        || Roles.SUPER_ADMIN.equals(target.getRole()))) {
//            throw new AccessDeniedException("ADMIN cannot deactivate admin accounts");
//        }
//
//        target.setActive(active);
//        authRepo.save(target);
//    }\

    public void changeUserStatus(Long authId, boolean active) {

        UserAuth auth = null;

        try {
            auth = authRepo.findById(authId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String loggedInEmail = getLoggedInEmail();

            // ❌ Prevent self status change
            if (auth.getEmail().equals(loggedInEmail)) {
                throw new AccessDeniedException("You cannot change your own account status");
            }

            // ❌ ADMIN cannot change ADMIN / SUPER_ADMIN
            if (isAdmin() &&
                    (auth.getRole() == Roles.ADMIN
                            || auth.getRole() == Roles.SUPER_ADMIN)) {
                throw new AccessDeniedException("ADMIN cannot change admin account status");
            }

            boolean beforeActive = auth.isActive();

            auth.setActive(active);
            authRepo.save(auth);

            ActionType actionType = active
                    ? ActionType.ACCOUNT_ACTIVATE
                    : ActionType.ACCOUNT_DEACTIVATE;

            // ✅ SUCCESS AUDIT
            actionAuditService.logAction(
                    actionType,
                    ActionStatus.SUCCESS,
                    auth.getEmail(),
                    auth.getId(),
                    String.valueOf(beforeActive),
                    String.valueOf(active),
                    active ? "Account activated" : "Account deactivated"
            );

        } catch (RuntimeException ex) {

            ActionType actionType = active
                    ? ActionType.ACCOUNT_ACTIVATE
                    : ActionType.ACCOUNT_DEACTIVATE;

            // ❌ FAILURE AUDIT
            actionAuditService.logFailure(
                    actionType,
                    auth != null ? auth.getEmail() : null,
                    auth != null ? auth.getId() : null,
                    ex.getMessage()
            );

            throw ex;
        }
    }


    // =====================================================
    // 🗑️ DELETE USER PROFILE (SOFT DELETE)
    // =====================================================

    public void deleteUserAndAuth(Long userId) {

        User user = null;


        try {
            String loggedInEmail = getLoggedInEmail();

            user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            UserAuth auth = user.getAuth();

            // ❌ Prevent self delete
            if (auth != null &&
                    auth.getEmail().equals(loggedInEmail)) {
                throw new AccessDeniedException("You cannot delete your own profile");
            }

            // ❌ ADMIN cannot delete ADMIN / SUPER_ADMIN
            if (isAdmin() &&
                    (auth.getRole() == Roles.ADMIN
                            || auth.getRole() == Roles.SUPER_ADMIN)) {
                throw new AccessDeniedException("ADMIN cannot delete admin accounts");
            }

            // Capture BEFORE state
            String beforeState = user.toString();

            // Delete user profile
            userRepo.delete(user);

            // Reset auth state

            if (auth != null) {
                auth.setProfileCreated(false);
                changeUserStatus(userId,false);
                authRepo.save(auth);
            }

            // ✅ SUCCESS AUDIT
            actionAuditService.logAction(
                    ActionType.USER_DELETE,
                    ActionStatus.SUCCESS,
                    auth.getEmail(),
                    auth.getId(),
                    beforeState,
                    "DELETED",
                    "User profile deleted"
            );

        } catch (RuntimeException ex) {

            // ❌ FAILURE AUDIT
            actionAuditService.logFailure(
                    ActionType.USER_DELETE,
                    user != null ? user.getAuth().getEmail() : null,
                    user != null ? user.getId() : null,
                    ex.getMessage()
            );

            throw ex;
        }
    }

}
