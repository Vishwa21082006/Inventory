package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(String username, String rawPassword, String role, String fullName) {
        User user = new User();
        user.setUsername(username);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setFullName(fullName);
        return userRepository.save(user);
    }

    public User loginUser(String username, String rawPassword) {
        // Recommended Java 8 style
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Check password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user; // frontend can use user.getId()
    }
}