package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.entity.PurchaseOrder;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.PurchaseOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Important for data safety

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository poRepo;

    @Autowired
    private ProductRepository productRepo;

    // 1. Create a new Order for the Supplier
    public void createPO(Long productId, int quantity) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setProduct(product);
        po.setQuantity(quantity);
        po.setStatus("ORDERED");
        po.setOrderDate(LocalDateTime.now());
        
        poRepo.save(po);
    }

    // 2. Receive the Shipment (The Magic Step)
    @Transactional // Ensures both DB updates happen, or neither happens
    public void receivePO(Long poId) {
        PurchaseOrder po = poRepo.findById(poId)
                .orElseThrow(() -> new RuntimeException("PO not found"));

        // Safety check: Don't receive the same order twice!
        if ("RECEIVED".equals(po.getStatus())) {
            return; 
        }

        // A. Mark PO as Received
        po.setStatus("RECEIVED");
        poRepo.save(po);

        // B. ACTUALLY INCREASE THE STOCK
        Product product = po.getProduct();
        int newStock = product.getStockQuantity() + po.getQuantity();
        product.setStockQuantity(newStock);
        
        productRepo.save(product); // Save the new stock level
    }

    // 3. Get list for the Admin Page
    public List<PurchaseOrder> getAllOrders() {
        return poRepo.findAll();
    }
}