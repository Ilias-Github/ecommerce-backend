package com.ecommerce.project.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Class die de login request representeert
// LoginRequestDTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    private String username;

    private String password;
}
