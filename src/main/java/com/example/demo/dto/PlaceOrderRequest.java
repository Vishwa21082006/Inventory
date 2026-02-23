package com.example.demo.dto;

import java.util.List;

public class PlaceOrderRequest {

    private Long userId;
    private List<CartItem> cartItems;

    // --- Getters and Setters ---
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
}