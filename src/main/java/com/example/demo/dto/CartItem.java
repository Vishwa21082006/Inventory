package com.example.demo.dto;

public class CartItem {
    private Long productId;
    private Integer quantity;

    // Getters and Setters are MANDATORY
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}