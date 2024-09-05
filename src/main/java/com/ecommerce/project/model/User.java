package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "app_user")
//@NoArgsConstructor
//@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    @Size(max = 20)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    //    @NotBlank
    @Size(max = 120)
    private String password;

    // Wanneer een user opgehaald wordt, wordt ook de role van de user opgehaald
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    // Een user kan een of meerdere producten aan zich gekoppeld hebben die verkocht worden op het platform
    // Orphan removal verwijdert alle producten die geen parent meer hebben
    // Dit gebeurt wanneer een user is verwijderd waarbij de producten alleen gelinkt waren aan die specifieke user
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<Product> products;

    @ToString.Exclude
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    Set<Address> addresses;

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Exclude van het user object bij het ophalen van de user
    @ToString.Exclude
    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private Cart cart;
}
