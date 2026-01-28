package com.example.registration.service;

import com.example.registration.entity.User;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.enums.Roles;
import com.example.registration.exception.AccessDeniedException;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.logging.BaseLogger;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService extends BaseLogger {

    private final UserAuthRepository authRepo;
    private final UserRepository userRepo;
    private final ActionAuditService actionAuditService;

    public AdminService(
            UserRepository userRepo,
            UserAuthRepository authRepo,
            ActionAuditService actionAuditService) {

        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.actionAuditService = actionAuditService;
    }

    // =====================================================
    // 🔐 COMMON HELPERS
    // =====================================================


    private String getLoggedInEmail() {
        return getAuthSafely().getName();
    }

    private String getLoggedInRole() {
        return getAuthSafely().getAuthorities()
                .iterator()
                .next()
                .getAuthority();
    }

    private boolean isAdmin() {
        return Roles.ADMIN.name().equals(getLoggedInRole());
    }

    private boolean isSuperAdmin() {
        return Roles.SUPER_ADMIN.name().equals(getLoggedInRole());
    }

    private org.springframework.security.core.Authentication getAuthSafely() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().isEmpty()) {
            throw new AccessDeniedException(ErrorMessages.UNAUTHORIZED);
        }
        return auth;
    }

    // =====================================================
    // 🔍 VIEW AUTH USERS
    // =====================================================

    public List<UserAuth> getAllAuthUsers() {

        log.info("Admin requested auth user list");

        return authRepo.findAll();
    }

    public void deleteLoginUser(Long authId) {

        log.info("Admin requested delete of auth record authId={}", authId);

        if (!authRepo.existsById(authId)) {
            log.warn("Auth delete failed - authId not found={}", authId);
            throw new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        authRepo.deleteById(authId);

        log.info("Auth record deleted successfully authId={}", authId);
    }

    // =====================================================
    // 🔐 CHANGE USER ROLE
    // =====================================================

    public void changeUserRole(Long authId, Roles newRole) {

        UserAuth target = null;

        try {
            log.info("Role change attempt authId={} newRole={}", authId, newRole);

            target = authRepo.findById(authId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

            if (target.getEmail().equals(getLoggedInEmail())) {
                log.warn("Role change denied - self role modification attempt");
                throw new AccessDeniedException("You cannot change your own role");
            }

            if (isAdmin()) {

                if (!Roles.USER.equals(target.getRole())
                        && !Roles.SUPPORT.equals(target.getRole())) {

                    log.warn("ADMIN attempted to modify restricted role authId={}", authId);
                    throw new AccessDeniedException("ADMIN cannot modify admin roles");
                }

                if (!Roles.USER.equals(newRole)
                        && !Roles.SUPPORT.equals(newRole)) {

                    log.warn("ADMIN attempted invalid role assignment authId={}", authId);
                    throw new AccessDeniedException("ADMIN can assign only USER or SUPPORT");
                }
            }

            if (isSuperAdmin() && !Roles.USER.equals(newRole)
                        && !Roles.SUPPORT.equals(newRole)
                        && !Roles.ADMIN.equals(newRole)
                        && !Roles.EDITOR.equals(newRole)) {

                    throw new AccessDeniedException("Invalid role assignment");
            }

            String before = target.getRole().name();

            target.setRole(newRole);
            authRepo.save(target);

            actionAuditService.logAction(
                    ActionType.ROLE_CHANGE,
                    ActionStatus.SUCCESS,
                    target.getEmail(),
                    target.getId(),
                    before,
                    newRole.name(),
                    "Role updated"
            );

            log.info("Role updated successfully authId={} from={} to={}",
                    authId, before, newRole);

        } catch (RuntimeException ex) {

            actionAuditService.logFailure(
                    ActionType.ROLE_CHANGE,
                    target != null ? target.getEmail() : null,
                    target != null ? target.getId() : null,
                    ex.getMessage()
            );

            log.error("Role change failed authId={}", authId);
            throw ex;
        }
    }

    // =====================================================
    // 🔐 ACTIVATE / DEACTIVATE LOGIN
    // =====================================================

    public void changeUserStatus(Long authId, boolean active) {

        UserAuth auth = null;

        try {
            log.info("Account status change attempt authId={} active={}", authId, active);

            auth = authRepo.findById(authId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

            if (auth.getEmail().equals(getLoggedInEmail())) {
                log.warn("Account status change denied - self modification");
                throw new AccessDeniedException("You cannot change your own account status");
            }

            if (isAdmin() &&
                    (auth.getRole() == Roles.ADMIN
                            || auth.getRole() == Roles.SUPER_ADMIN)) {

                log.warn("ADMIN attempted to change admin account status authId={}", authId);
                throw new AccessDeniedException("ADMIN cannot change admin account status");
            }

            boolean beforeActive = auth.isActive();

            auth.setActive(active);
            authRepo.save(auth);

            ActionType actionType = active
                    ? ActionType.ACCOUNT_ACTIVATE
                    : ActionType.ACCOUNT_DEACTIVATE;

            actionAuditService.logAction(
                    actionType,
                    ActionStatus.SUCCESS,
                    auth.getEmail(),
                    auth.getId(),
                    String.valueOf(beforeActive),
                    String.valueOf(active),
                    active ? "Account activated" : "Account deactivated"
            );

            log.info("Account status updated authId={} from={} to={}",
                    authId, beforeActive, active);

        } catch (RuntimeException ex) {

            ActionType actionType = active
                    ? ActionType.ACCOUNT_ACTIVATE
                    : ActionType.ACCOUNT_DEACTIVATE;

            actionAuditService.logFailure(
                    actionType,
                    auth != null ? auth.getEmail() : null,
                    auth != null ? auth.getId() : null,
                    ex.getMessage()
            );

            log.error("Account status change failed authId={}", authId);
            throw ex;
        }
    }

    // =====================================================
    // 🗑️ DELETE USER PROFILE
    // =====================================================

    public void deleteUserAndAuth(Long userId) {

        User user = null;

        try {
            log.info("User delete attempt userId={}", userId);

            String loggedInEmail = getLoggedInEmail();

            user = userRepo.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

            UserAuth auth = user.getAuth();

            if (auth == null) {
                log.error("UserAuth missing for userId={}", userId);
                throw new IllegalStateException("User authentication data missing");
            }

            if (auth.getEmail().equals(loggedInEmail)) {
                log.warn("User delete denied - self delete attempt");
                throw new AccessDeniedException("You cannot delete your own profile");
            }

            if (isAdmin() &&
                    (auth.getRole() == Roles.ADMIN
                            || auth.getRole() == Roles.SUPER_ADMIN)) {

                log.warn("ADMIN attempted to delete admin account userId={}", userId);
                throw new AccessDeniedException(ErrorMessages.ADMIN_CANNOT_DELETE_ADMIN);
            }

            String beforeState = user.toString();

            userRepo.delete(user);

           auth.setProfileCreated(false);
           changeUserStatus(userId, false);
           authRepo.save(auth);


            actionAuditService.logAction(
                    ActionType.USER_DELETE,
                    ActionStatus.SUCCESS,
                    auth.getEmail(),
                    auth.getId(),
                    beforeState,
                    "DELETED",
                    "User profile deleted"
            );

            log.info("User deleted successfully userId={}", userId);

        } catch (RuntimeException ex) {

            actionAuditService.logFailure(
                    ActionType.USER_DELETE,
                    user != null ? user.getAuth().getEmail() : null,
                    user != null ? user.getId() : null,
                    ex.getMessage()
            );

            log.error("User delete failed userId={}", userId);
            throw ex;
        }
    }

    public final class ErrorMessages {

        private ErrorMessages() {}

        public static final String USER_NOT_FOUND = "User not found";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String ADMIN_CANNOT_DELETE_ADMIN =
                "ADMIN cannot delete admin accounts";
    }
}
