package com.ecommerce.project.controller;

import com.ecommerce.project.model.ERole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.IRoleRepository;
import com.ecommerce.project.repositories.IUserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.jwt.payload.LoginRequest;
import com.ecommerce.project.security.jwt.payload.MessageResponse;
import com.ecommerce.project.security.jwt.payload.SignUpRequest;
import com.ecommerce.project.security.jwt.payload.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    IUserRepository userRepository;

    @Autowired
    IRoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;

        try {
            authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
                    );
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        List<String> roles =
                userDetails.getAuthorities().stream().map(item -> item.getAuthority()).toList();

        UserInfoResponse response =
                new UserInfoResponse(userDetails.getId(), jwtToken, userDetails.getUsername(), roles);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        // Check of username bestaat
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            // Error die teruggegeven wordt aan de client
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken"));
        }

        // Check of email bestaat
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            // Error die teruggegeven wordt aan de client
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use"));
        }

        // Account creatie aan de hand van de User model
        // Wachtwoord moet encode worden voordat deze opgeslagen wordt in de database
        User user = new User(
                signUpRequest.getUsername(), signUpRequest.getEmail(), passwordEncoder.encode(signUpRequest.getPassword())
        );

        // De client verstuurt de roles in de vorm van een leesbare string. Deze dient omgezet te worden naar de enum
        // die wij geschreven hebben
        Set<String> strRoles = signUpRequest.getRoles();

        // De roles hoe ze daadwerkelijk in de database moeten worden opgeslagen
        Set<Role> roles = new HashSet<>();

        // Als bij het inschrijven geen role wordt meegegeven, dan wordt standaard de user role aangehouden
        // Als strRoles null is kan je er niet doorheen itereren, daarom moet deze if check gebouwd worden
        if (strRoles == null) {
            Role userRole = roleRepository.findByRoleName(ERole.USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            // Een user kan meerdere rollen hebben. Dus je moet door de meegegeven rollen itereren
            strRoles.forEach(role -> {
                switch (role) {
                    case "seller":
                        Role sellerRole = roleRepository.findByRoleName(ERole.SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(sellerRole);
                        break;
                    case "admin":
                        Role adminRole = roleRepository.findByRoleName(ERole.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(ERole.USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                        break;
                }
            });
        }

        // Sla de roles op die zojuist zijn aangemaakt in de user model
        user.setRoles(roles);

        // Sla de user op in de database
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered succesfully"));
    }
}
