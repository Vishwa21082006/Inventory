package com.example.demo.controller;

import com.example.demo.dto.PasswordChangeRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")  
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Returns current logged-in user's details
    @GetMapping("/users/current-user")
    public ResponseEntity<User> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }

    // Secure password change endpoint
    @PostMapping("/user/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody PasswordChangeRequest request,
            Principal principal) {

        try {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body("Incorrect current password!");
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return ResponseEntity.ok("Password updated successfully!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Updates the Admin's display name
    @PostMapping("/user/update-name")
    public ResponseEntity<?> updateName(
            @RequestBody Map<String, String> request,
            Principal principal) {

        try {
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String newName = request.get("newName");
            if (newName == null || newName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("New name cannot be empty");
            }

            user.setFullName(newName.trim());
            userRepository.save(user);

            return ResponseEntity.ok("Name updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Fetch all Staff (Admins)
    @GetMapping("/users/staff")
    public ResponseEntity<?> getStaff() {
        List<User> staff = userRepository.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRole()) || "ROLE_ADMIN".equals(u.getRole()))
                .toList();
        return ResponseEntity.ok(staff);
    }

    // Securely create a new Admin account
    @PostMapping("/users/add-staff")
    public ResponseEntity<?> addStaff(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }

        User newAdmin = new User();
        newAdmin.setUsername(request.getUsername());
        newAdmin.setPassword(passwordEncoder.encode(request.getPassword()));
        newAdmin.setFullName(request.getFullName());
        newAdmin.setRole("ADMIN"); // Force Admin Role
        
        userRepository.save(newAdmin);
        return ResponseEntity.ok("Staff account created successfully!");
    }

    // 🟢 NEW: Securely delete a Staff account
    @DeleteMapping("/users/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Long id, Principal principal) {
        try {
            User userToDelete = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Safety Check: You cannot delete yourself!
            if (userToDelete.getUsername().equals(principal.getName())) {
                return ResponseEntity.badRequest().body("You cannot delete your own account!");
            }

            userRepository.delete(userToDelete);
            return ResponseEntity.ok("Staff member removed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // DTO for capturing registration data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String role;
        private String fullName;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }
}