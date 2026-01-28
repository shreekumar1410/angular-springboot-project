package com.example.registration.service;

import com.example.registration.dto.UserFullResponse;
import com.example.registration.dto.UserProfileRequest;
import com.example.registration.dto.UserShortResponse;
import com.example.registration.dto.UserViewResponse;
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
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService extends BaseLogger {

    private final UserRepository userRepo;
    private final UserAuthRepository authRepo;
    private final ActionAuditService actionAuditService;

    public UserService(
            UserRepository userRepo,
            UserAuthRepository authRepo,
            ActionAuditService actionAuditService) {

        this.userRepo = userRepo;
        this.authRepo = authRepo;
        this.actionAuditService = actionAuditService;
    }

    private org.springframework.security.core.Authentication getAuthSafely() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().isEmpty()) {
            throw new AccessDeniedException("Unauthorized");
        }
        return auth;
    }

    /* ================= FIRST-TIME PROFILE ================= */

    public User createProfile(Long authId, UserProfileRequest request) {

        UserAuth auth = null;

        try {
            String loggedInEmail = getAuthSafely().getName();

            String role = getAuthSafely().getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority();

            log.info("Profile creation attempt by role={} for authId={}", role, authId);

            auth = authRepo.findById(authId)
                    .orElseThrow(() -> new AccessDeniedException("Unauthorized"));

            if (Roles.USER.name().equals(role) &&
                    !auth.getEmail().equals(loggedInEmail)) {

                log.warn(
                        "Profile creation denied: USER tried to create profile for another user authId={}",
                        authId
                );
                throw new AccessDeniedException("You can create only your own profile");
            }

            if (auth.isProfileCreated()) {
                log.warn("Profile already exists for authId={}", authId);
                throw new AccessDeniedException("Profile already created");
            }

            User user = new User();
            user.setName(request.getName());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());
            user.setDob(request.getDob());
            user.setLanguages(request.getLanguages());
            user.setGender(request.getGender());
            user.setAge(request.getAge());
            user.setEmailId(auth.getEmail());
            user.setQualification(request.getQualification());
            user.setAuth(auth);

            User savedUser = userRepo.save(user);

            auth.setProfileCreated(true);
            authRepo.save(auth);

            actionAuditService.logAction(
                    ActionType.PROFILE_CREATE,
                    ActionStatus.SUCCESS,
                    auth.getEmail(),
                    savedUser.getId(),
                    null,
                    savedUser.toString(),
                    "Profile created"
            );

            log.info("Profile created successfully for authId={}", authId);

            return savedUser;

        } catch (RuntimeException ex) {

            actionAuditService.logFailure(
                    ActionType.PROFILE_CREATE,
                    auth != null ? auth.getEmail() : null,
                    auth != null ? auth.getId() : null,
                    ex.getMessage()
            );

            log.error("Profile creation failed for authId={}", authId);

            throw ex;
        }
    }

    public User createProfileByEditor(Long authId, UserProfileRequest request) {

        String role = getAuthSafely().getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        log.info("Create profile by editor attempt for authId={}, role={}", authId, role);

        if (Roles.EDITOR.name().equals(role)) {
            return createProfile(authId, request);
        }

        log.warn("Non-editor attempted profile creation for authId={}", authId);
        throw new AccessDeniedException("Only EDITOR can create profiles for users");
    }

    /* ================= USER LIST ================= */

    public List<UserViewResponse> getUsersByRole() {

        var authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().isEmpty()) {

            log.warn("User list request failed - unauthenticated or no authorities");
            throw new AccessDeniedException("Unauthorized");
        }

        String role = authentication.getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        log.info("User list requested by role={}", role);

        List<User> users = userRepo.findAll();

        if (Roles.USER.name().equals(role)) {

            AtomicLong sno = new AtomicLong(1);

            return users.stream()
                    .map(u -> (UserViewResponse) new UserShortResponse(
                            sno.getAndIncrement(),
                            u.getName(),
                            u.getAuth().getEmail(),
                            maskPhone(u.getPhone())
                    ))
                    .toList();
        }

        return users.stream()
                .map(u -> (UserViewResponse) new UserFullResponse(
                        u.getId(),
                        u.getName(),
                        u.getAuth().getEmail(),
                        u.getPhone(),
                        u.getAddress(),
                        u.getDob(),
                        u.getLanguages()
                ))
                .toList();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "XXXX";
        return "XXXXXX" + phone.substring(phone.length() - 4);
    }

    /* ================= PROFILE CRUD ================= */

    public User getUserById(Long id) {

        log.info("Fetching profile for userId={}", id);

        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateUser(Long id, User user) {

        User existing = null;

        try {
            existing = getUserById(id);

            String loggedInEmail = getAuthSafely().getName();

            String role = getAuthSafely().getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority();

            String profileOwnerEmail = existing.getAuth().getEmail();

            log.info(
                    "Profile update attempt by role={} for userId={}",
                    role, id
            );

            if (Roles.USER.name().equals(role) &&
                    !profileOwnerEmail.equals(loggedInEmail)) {

                log.warn(
                        "Profile update denied: USER tried to edit another profile userId={}",
                        id
                );
                throw new AccessDeniedException("You can edit only your own profile");
            }

            String before = existing.toString();

            existing.setName(user.getName());
            existing.setPhone(user.getPhone());
            existing.setAddress(user.getAddress());
            existing.setDob(user.getDob());
            existing.setLanguages(user.getLanguages());
            existing.setGender(user.getGender());
            existing.setAge(user.getAge());
            existing.setEmailId(existing.getAuth().getEmail());
            existing.setQualification(user.getQualification());

            User updated = userRepo.save(existing);

            actionAuditService.logAction(
                    ActionType.PROFILE_UPDATE,
                    ActionStatus.SUCCESS,
                    existing.getAuth().getEmail(),
                    existing.getId(),
                    before,
                    updated.toString(),
                    "Profile updated"
            );

            log.info("Profile updated successfully for userId={}", id);

            return updated;

        } catch (RuntimeException ex) {

            actionAuditService.logFailure(
                    ActionType.PROFILE_UPDATE,
                    existing != null ? existing.getAuth().getEmail() : null,
                    existing != null ? existing.getId() : null,
                    ex.getMessage()
            );

            log.error("Profile update failed for userId={}", id);

            throw ex;
        }
    }

    public User getMyProfile() {

        String email = getAuthSafely().getName();

        log.info("Fetching profile for logged-in user email={}", email);

        return userRepo.findByAuthEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profile not found for logged-in user"));
    }
}
