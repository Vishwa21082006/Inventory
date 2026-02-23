package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Print who we are looking for
        System.out.println("🔍 Attempting to load user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ User NOT found in Database!"); 
                    return new UsernameNotFoundException("User not found");
                });

        // 2. Print exactly what we found in the database
        System.out.println("✅ User Found: " + user.getUsername());
        System.out.println("🔑 Password Hash in DB: " + user.getPassword());
        System.out.println("🛡️ Role: " + user.getRole());

        // 3. Return the user to Spring Security
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}