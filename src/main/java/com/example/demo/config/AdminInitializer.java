package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;  // 🟢 For cryptographically secure randomness

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if an admin already exists to avoid duplicates
        if (userRepository.findByUsername("admin").isEmpty()) {
            // 🟢 Generate a secure random password
            String randomPassword = generateSecurePassword(16);  // 16 chars: strong and memorable

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(randomPassword));  // Hash it securely
            admin.setFullName("Master Admin");
            admin.setEmail("admin@store.com");
            admin.setPhone("0000000000");
            admin.setRole("ADMIN");  // Securely set to ADMIN

            userRepository.save(admin);

            // 🟢 Log the credentials to console ONLY (ephemeral, secure for handover)
            System.out.println("✅ MASTER ADMIN ACCOUNT CREATED SUCCESSFULLY");
            System.out.println("Username: admin");
            System.out.println("Password: " + randomPassword);  // Print plain text once
            System.out.println("⚠️ IMPORTANT: Log in immediately and change this password! Do not share it.");
        }
    }

    // 🟢 Helper method to generate a strong, random password
    private String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}