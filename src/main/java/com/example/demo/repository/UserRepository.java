package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // <--- This import is important!

public interface UserRepository extends JpaRepository<User, Long> {
    // Return Optional<User> so we can use .orElse() in the Service
    Optional<User> findByUsername(String username);
 
}