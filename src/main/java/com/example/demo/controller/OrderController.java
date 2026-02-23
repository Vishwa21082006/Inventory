package com.example.demo.controller;

import com.example.demo.dto.PlaceOrderRequest;
import com.example.demo.entity.Order;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PdfService pdfService;

    // 1. Place a new order (improved version with error handling)
    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody PlaceOrderRequest request) {
        try {
            Order newOrder = orderService.placeOrder(request.getCartItems());
            return ResponseEntity.ok(newOrder); // 200 OK + created order
        } catch (RuntimeException e) {
            // Business/validation errors → out of stock, invalid quantity, etc.
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Unexpected server-side issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred while placing your order"));
        }
    }

    // 2. ADMIN: Update order status & expected delivery date
    @PostMapping("/{orderId}/update-tracking")
    public Order updateTracking(@PathVariable Long orderId,
                                @RequestParam String status,
                                @RequestParam String date) {
        return orderService.updateTracking(orderId, status, date);
    }

    // 3. Get all orders for a specific user (used in user dashboard / bags tab)
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderRepository.findByCustomer_Id(userId);
    }

    // 4. Get pending orders (for admin dashboard red badge / notification)
    @GetMapping("/pending")
    public List<Order> getPendingOrders() {
        return orderService.getPendingOrders();
    }

    // 5. Dashboard statistics (admin)
    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        return orderService.getDashboardStats();
    }

    // 6. Customer confirms they received the order
    @PostMapping("/{orderId}/confirm-delivery")
    public Order confirmDelivery(@PathVariable Long orderId) {
        return orderService.confirmDelivery(orderId);
    }

    // 7. Customer requests a return
    @PostMapping("/{orderId}/return")
    public Order requestReturn(@PathVariable Long orderId, @RequestBody String reason) {
        return orderService.requestReturn(orderId, reason);
    }

    // 8. Admin approves a return request
    @PostMapping("/{orderId}/approve-return")
    public Order approveReturn(@PathVariable Long orderId) {
        return orderService.approveReturn(orderId);
    }

    // 9. Customer's "My Orders" page (returns view name → ideally move to a separate @Controller later)
    @GetMapping("/my-orders")
    public String getMyOrders(Model model, Principal principal) {
        String currentUsername = principal.getName();
        List<Order> myOrders = orderRepository.findByCustomer_Username(currentUsername);
        model.addAttribute("orders", myOrders);
        return "my-orders"; // Thymeleaf template name
    }

    // 10. Download invoice PDF for a specific order
    @GetMapping(value = "/{id}/invoice", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<InputStreamResource> downloadInvoice(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));

        ByteArrayInputStream pdfStream = pdfService.generateInvoice(order);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=invoice_order_" + id + ".pdf");
        // Change to "attachment;" if you want force-download instead of preview

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }
}