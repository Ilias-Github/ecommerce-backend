package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    private String streetName;

    @Min(value = 1, message = "House number cant be 0 or negative")
    @Max(value = 9999, message = "House number cant exceed 9999")
    private int houseNumber;

    private String apartmentNumber;

    @NotBlank
    @Size(min = 6, max = 6, message = "Zipcode must be exactly 6 characters")
    private String zipcode;

    @NotBlank
    @Size(min = 5, message = "City name must be at least 5 characters")
    private String city;

    // TODO: Alle lists initaliseren?
    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private List<User> users;

    public Address(String streetName, int houseNumber, String apartmentNumber, String zipcode, String city) {
        this.streetName = streetName;
        this.houseNumber = houseNumber;
        this.apartmentNumber = apartmentNumber;
        this.zipcode = zipcode;
        this.city = city;
    }
}
