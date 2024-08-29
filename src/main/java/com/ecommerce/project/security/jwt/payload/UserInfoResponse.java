package com.ecommerce.project.security.jwt.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// De response die terug wordt verwacht na het inloggen is de JWT token, de username en de rollen van de gebruiker.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String jwtToken;
    private String username;
    private List<String> roles;
}
