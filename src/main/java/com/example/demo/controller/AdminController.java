package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.entity.PurchaseOrder;
import com.example.demo.service.PurchaseOrderService;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private PurchaseOrderService poService;

    @Autowired
    private ProductRepository productRepository;

    // 1. DELETE: We removed the "showSupplierPage" method because you deleted the HTML file.

    // 2. NEW: API to send PO History to the Dashboard Modal (JSON data)
    @GetMapping("/api/pos")
    @ResponseBody
    public List<PurchaseOrder> getPOHistory() {
        return poService.getAllOrders();
    }

    // 3. Handle "Create Order" (Redirects back to Dashboard)
    @PostMapping("/po/create")
    public String createPO(@RequestParam Long productId, @RequestParam int quantity) {
        poService.createPO(productId, quantity);
        return "redirect:/admin.html"; // Go back to main dashboard
    }

    // 4. Handle "Receive Shipment" (Redirects back to Dashboard)
    @PostMapping("/po/receive/{id}")
    public String receivePO(@PathVariable Long id) {
        poService.receivePO(id);
        return "redirect:/admin.html"; // Go back to main dashboard
    }
}