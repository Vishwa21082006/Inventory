package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize on methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 1. PUBLIC PAGES (Open to everyone)
                .requestMatchers(
                    "/", 
                    "/index.html", 
                    "/login.html", 
                    "/register.html", 
                    "/register",      // Kept safe!
                    "/css/**", 
                    "/js/**", 
                    "/images/**",
                    "/uploads/**"     // Images won't break!
                ).permitAll()
                
                // Allow public viewing of products (GET only)
                .requestMatchers(HttpMethod.GET, "/products/**").permitAll()
                
                // Product modifications → ADMIN only
                .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/products/**").hasRole("ADMIN")
                
                // Admin dashboard & APIs
                .requestMatchers("/admin.html", "/admin/**", "/api/admin/**").hasRole("ADMIN")
                
                // Customer actions (cart, orders, checkout)
                .requestMatchers("/cart/**", "/checkout/**", "/orders/**").hasAnyRole("CUSTOMER", "ADMIN")
                
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html") 
                .loginProcessingUrl("/login") 
                .successHandler(myCustomSuccessHandler()) 
                .failureUrl("/login.html?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/index.html")
                .permitAll()
            )
            // MUST keep CSRF disabled until Javascript is updated with tokens
            .csrf(csrf -> csrf.disable()) 
            
            // Your new awesome security headers!
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(content -> content.disable())
                .xssProtection(xss -> xss.disable())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(31536000)
                )
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler myCustomSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String role = authorities.stream().findFirst().get().getAuthority();

            if (role.equals("ROLE_ADMIN")) {    
                response.sendRedirect("/admin.html"); 
            } else {
                response.sendRedirect("/index.html"); 
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Your stronger level 12 encoder!
        return new BCryptPasswordEncoder(12); 
    }   
}