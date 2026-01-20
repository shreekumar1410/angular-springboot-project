
package com.example.registration.service;

import com.example.registration.dto.UserFullResponse;
import com.example.registration.dto.UserProfileRequest;
import com.example.registration.dto.UserShortResponse;
import com.example.registration.entity.User;
import com.example.registration.entity.UserAuth;
import com.example.registration.enums.ActionStatus;
import com.example.registration.enums.ActionType;
import com.example.registration.exception.AccessDeniedException;
import com.example.registration.exception.ResourceNotFoundException;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import static com.example.registration.enums.Roles.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final UserAuthRepository authRepo;
    ActionAuditService actionAuditService;

    public UserService(UserRepository userRepo, UserAuthRepository authRepo) {
        this.userRepo = userRepo;
        this.authRepo = authRepo;
    }

    /* ================= FIRST-TIME PROFILE ================= */

    public User createProfile(Long authId, UserProfileRequest request) {

        String loggedInEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        String role = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority();
//
//        System.out.println("LOGIN MAIL");
//        System.out.println("email = " + email);

        UserAuth auth = authRepo.findById(authId)
                .orElseThrow(() -> new AccessDeniedException("Unauthorized"));

        // USER restriction
        if ("USER".equals(role) && !auth.getEmail().equals(loggedInEmail)) {
            throw new AccessDeniedException("You can create only your own profile");
        }

        if (auth.isProfileCreated()) {
            throw new AccessDeniedException("Profile already created");
        }
//
//        System.out.println("this is from user service");
//        System.out.println(request.getEmailId());

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

        //for action aduit
        actionAuditService.logAction(
                ActionType.PROFILE_CREATE,
                ActionStatus.SUCCESS,
                auth.getEmail(),
                user.getId(),
                null,
                user.toString(), // or JSON
                "Profile created"
        );

        return savedUser;
    }

    /* ================= EXISTING CRUD (FOR CONTROLLER) ================= */

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    public List<?> getUsersByRole() {

        String role = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        List<User> users = userRepo.findAll();

        System.out.println("role = " + role);


        if (USER.equals(role)) {
        // USER → SHORT INFO

            System.out.println("this sout is used in userservice");
            System.out.println("CURRENT USER ROLE: "+role);

            AtomicLong sno = new AtomicLong(1);
            return users.stream()
                    .map(u -> new UserShortResponse(
                            sno.getAndIncrement(),
                            u.getName(),
                            u.getAuth().getEmail(),
                            maskPhone(u.getPhone())
                    ))
                    .toList();

        } else {
            // ADMIN + SUPER_ADMIN → FULL INFO

            System.out.println("this sout is used in userservice");
            System.out.println("CURRENT USER ROLE: "+role);


            return users.stream()
                    .map(u -> new UserFullResponse(
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
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "XXXX";
        return "XXXXXX" + phone.substring(phone.length() - 4);
    }


    public User getUserById(Long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User updateUser(Long id, User user) {

        // 1. Fetch existing profile
        User existing = getUserById(id);

        // 2. Get logged-in user's email from JWT
        String loggedInEmail =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        String role = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .iterator()
                .next()
                .getAuthority();

        String targetRole = existing.getAuth().getRole().name();

//        System.out.println("this is from the user service...");
//        System.out.println("loggedInEmail = " + loggedInEmail);
//        System.out.println("role = " + role);
//        System.out.println("targetRole = " + targetRole);

        // 3. Get profile owner's email
        String profileOwnerEmail =
                existing.getAuth().getEmail();

        // USER restriction
        if ("USER".equals(role) &&
                !profileOwnerEmail.equals(loggedInEmail)) {

            throw new AccessDeniedException("You can edit only your own profile");
        }

        // EDITOR restriction
        if ("EDITOR".equals(role) &&
                ("ADMIN".equals(targetRole) || "SUPER_ADMIN".equals(targetRole))) {

            throw new AccessDeniedException(
                    "You cannot edit ADMIN or SUPER_ADMIN profiles"
            );
        }

        //for action aduit
        String before = existing.toString();

        // 5. Update allowed fields
        existing.setName(user.getName());
        existing.setPhone(user.getPhone());
        existing.setAddress(user.getAddress());
        existing.setDob(user.getDob());
        existing.setLanguages(user.getLanguages());
        existing.setGender(user.getGender());
        existing.setAge(user.getAge());
//        System.out.println("this sout is at userservice.");
//        System.out.println("USer Email = "+existing.getAuth().getEmail());
        existing.setEmailId(existing.getAuth().getEmail());
        existing.setQualification(user.getQualification());

        //for action aduit
        actionAuditService.logAction(
                ActionType.PROFILE_UPDATE,
                ActionStatus.SUCCESS,
                existing.getAuth().getEmail(),
                existing.getId(),
                before,
                existing.toString(),
                "Profile updated"
        );


        return userRepo.save(existing);
    }


    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new ResourceNotFoundException("User not found");
        }
        userRepo.deleteById(id);
    }


    public User getMyProfile() {

        // Get logged-in email from JWT
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        System.out.println(" lOGIN Emial = "+email);

        return userRepo.findByAuthEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Profile not found for logged-in user"));
    }
}
