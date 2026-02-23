package com.example.demo.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.dto.ProductRequest;
import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 🟢 NEW: Grabs the Cloudinary URL from application.properties
    @Value("${cloudinary.url}")
    private String cloudinaryUrl;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setSkuCode(request.getSkuCode());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setTargetStock(request.getTargetStock());
        product.setIsActive(true);
        product.setImageUrl("https://via.placeholder.com/150");

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable Long id,
            @RequestParam int quantity,
            @RequestParam int target) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setStockQuantity(quantity);
        product.setTargetStock(target);
        Product updated = productRepository.save(product);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productRepository.deleteById(id);
            return ResponseEntity.ok("Product deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: Cannot delete this product. It might be linked to existing orders!");
        }
    }

    // 🟢 FIXED: Now uploads directly to Cloudinary instead of local folder!
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            // 1. Initialize Cloudinary
            Cloudinary cloudinary = new Cloudinary(cloudinaryUrl);

            // 2. Upload the file directly to the cloud
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            // 3. Get the permanent secure URL from Cloudinary
            String secureImageUrl = uploadResult.get("secure_url").toString();

            // 4. Save the cloud URL to your database
            product.setImageUrl(secureImageUrl);
            productRepository.save(product);

            return ResponseEntity.ok(product);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Failed to upload image: " + e.getMessage());
        }
    }
}