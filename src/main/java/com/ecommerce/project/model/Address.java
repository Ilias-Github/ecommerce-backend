package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    @NotBlank
    @Size(max = 4, message = "House number should not exceed 4 characters")
    private int houseNumber;

    private String apartmentNumber;

    @NotBlank
    @Size(min = 6, max = 6, message = "Zipcode must be 6 characters")
    private String zipcode;

    @NotBlank
    @Size(min = 5, message = "City name must be at least 5 characters")
    private String city;

    @ToString.Exclude
    @ManyToMany
    private List<User> users;

    public Address(String street, int houseNumber, String apartmentNumber, String zipcode, String city) {
        this.street = street;
        this.houseNumber = houseNumber;
        this.apartmentNumber = apartmentNumber;
        this.zipcode = zipcode;
        this.city = city;
    }
}
