package com.example.demo.service;

import com.example.demo.dto.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    // --- 1. PLACE ORDER (Updated to accept List<CartItem>) ---
    @Transactional
    public Order placeOrder(List<CartItem> cartItems) {
        Order newOrder = new Order();
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setTrackingStatus("Ordered");
        newOrder.setItems(new ArrayList<>());

        // Link User (handles guest/anonymous case)
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username != null && !username.equals("anonymousUser")) {
            User currentUser = userRepository.findByUsername(username).orElse(null);
            newOrder.setCustomer(currentUser);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Long productId = item.getProductId();
            Integer quantity = item.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Out of stock: " + product.getName());
            }

            // Decrease stock
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(newOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);

            // Handle price safely
            BigDecimal price = (product.getPrice() instanceof BigDecimal)
                    ? (BigDecimal) product.getPrice()
                    : BigDecimal.valueOf(((Number) product.getPrice()).doubleValue());

            orderItem.setPriceAtPurchase(price);
            newOrder.getItems().add(orderItem);

            total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
        }

        newOrder.setTotalAmount(total.doubleValue());
        return orderRepository.save(newOrder);
    }

    // --- 2. DASHBOARD STATS ---
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        List<Product> allProducts = productRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();

        int totalStock = allProducts.stream().mapToInt(Product::getStockQuantity).sum();

        long pendingCount = allOrders.stream()
                .filter(o -> "Ordered".equals(o.getTrackingStatus()) || "Shipped".equals(o.getTrackingStatus()))
                .count();

        long deliveredCount = allOrders.stream()
                .filter(o -> "Delivered".equals(o.getTrackingStatus()))
                .count();

        double totalRevenue = allOrders.stream()
                .filter(o -> !"Returned".equals(o.getTrackingStatus()))
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0.0)
                .sum();

        stats.put("totalStock", totalStock);
        stats.put("pendingOrders", pendingCount);
        stats.put("deliveredOrders", deliveredCount);
        stats.put("totalRevenue", totalRevenue);

        return stats;
    }

    // ────────────────────────────────────────────────
    //          HELPER / STATE CHANGE METHODS
    // ────────────────────────────────────────────────

    // 🟢 Customer clicks "I Received This Order"
    public Order confirmDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Change status to Delivered so Admin can print the invoice, etc.
        order.setTrackingStatus("Delivered");

        // Record the EXACT time the customer confirmed receipt
        order.setDeliveryDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    // 🟢 Admin clicks "Ship" or "Deliver" from the dashboard
    public Order updateTracking(Long orderId, String status, String dateStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setTrackingStatus(status);

        // Handle expected / actual delivery date
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                // Parse "YYYY-MM-DD" and set it to the start of that day
                order.setDeliveryDate(java.time.LocalDate.parse(dateStr).atStartOfDay());
            } catch (Exception e) {
                System.out.println("Warning: Could not parse date string: " + dateStr);
                // You could also throw or log more formally here in production
            }
        } else if ("Delivered".equals(status)) {
            // If Admin manually marks as Delivered, use current time
            order.setDeliveryDate(LocalDateTime.now());
        }
        // Note: for "Shipped" without date → deliveryDate stays null or previous value

        return orderRepository.save(order);
    }

    public Order requestReturn(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.setTrackingStatus("Return Requested");
        order.setReturnReason(reason);
        return orderRepository.save(order);
    }

    @Transactional
    public Order approveReturn(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if ("Returned".equals(order.getTrackingStatus())) return order;

        order.setTrackingStatus("Returned");

        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        return orderRepository.save(order);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findAll(); // ← consider adding a proper query later
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}