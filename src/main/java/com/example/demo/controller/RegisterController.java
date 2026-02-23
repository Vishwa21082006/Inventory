package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // ⚠️ CRITICAL: Injects the security tool

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone) {

        // 1. Check if username exists
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/register.html?error=exists";
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        
        // 2. FORCE Role to CUSTOMER (So nobody can register as Admin)
        newUser.setRole("CUSTOMER"); 

        // 3. ENCRYPT the Password (So Login works later)
        newUser.setPassword(passwordEncoder.encode(password)); 

        userRepository.save(newUser);

        return "redirect:/login.html?success=true";
    }
}