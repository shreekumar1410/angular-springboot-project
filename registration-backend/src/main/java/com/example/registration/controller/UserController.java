//package com.example.registration.controller;
//
//
//import com.example.registration.entity.User;
//import com.example.registration.service.UserService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/users")
//@CrossOrigin("*")
//public class UserController {
//
//    private final UserService service;
//
//    public UserController(UserService service){
//        this.service=service;
//    }
//
//    @PostMapping
//    public User createUser(@RequestBody User user){
//        return service.saveUser(user);
//    }
//
//    @GetMapping
//    public List<User> getUser(){
//        return service.getAllUsers();
//    }
//
//    @GetMapping("/{id}")
//    public User getUserById(@PathVariable Long id) {
//        return service.getUserById(id);
//    }
//
//    @PutMapping("/{id}")
//    public User updateUser(@PathVariable Long id, @RequestBody User user){
//        return service.updateUser(id, user);
//    }
//
//   @DeleteMapping("/{id}")
//   public ResponseEntity<?> delectUser(@PathVariable Long id){
//        service.deleteUser(id);
//        return ResponseEntity.ok().build();
//    }
//
//
//
//}


package com.example.registration.controller;

import com.example.registration.dto.UserProfileRequest;
import com.example.registration.entity.User;
import com.example.registration.entity.UserAuth;
import com.example.registration.exception.AccessDeniedException;
import com.example.registration.repository.UserAuthRepository;
import com.example.registration.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
public class UserController {

    private final UserService service;
    private final UserAuthRepository authRepo;

    public UserController(UserService service, UserAuthRepository authRepo) {
        this.service = service;
        this.authRepo = authRepo;
    }

    /**
     * CREATE PROFILE (FIRST TIME AFTER LOGIN)
     */
    @PostMapping
    public User createProfile(@RequestBody UserProfileRequest request) {

        // 1️⃣ Get logged-in email from JWT
        String email =
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName();

        // 2️⃣ Fetch auth record
        UserAuth auth = authRepo.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Unauthorized"));

        // 3️⃣ Allow profile creation only once
        if (auth.isProfileCreated()) {
            throw new AccessDeniedException("Profile already created");
        }

        // 4️⃣ Create profile via service
        return service.createProfile(auth.getId(), request);
    }

    /**
     * ADMIN & USER VIEW (will be refined in Step 2)
     */
    @GetMapping
    public List<?> getUser() {
        return service.getUsersByRole();
    }

//    @GetMapping("/{id}")
//    public User getUserById(@PathVariable Long id) {
//        return service.getUserById(id);
//    }

    /**
     * UPDATE OWN PROFILE ONLY (ADMIN + USER)
     */
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user) {

        return service.updateUser(id, user);
    }

    @GetMapping("/me")
    public User getMyProfile() {
        return service.getMyProfile();
    }
}
