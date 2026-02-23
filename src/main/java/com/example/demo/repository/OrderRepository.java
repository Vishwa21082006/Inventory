package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Finds orders by Customer ID
    List<Order> findByCustomer_Id(Long userId);

    // Finds orders by Customer Username
    List<Order> findByCustomer_Username(String username);
}