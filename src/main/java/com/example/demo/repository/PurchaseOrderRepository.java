package com.example.demo.repository;

import com.example.demo.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    // Find all orders that haven't arrived yet
    List<PurchaseOrder> findByStatus(String status);
}