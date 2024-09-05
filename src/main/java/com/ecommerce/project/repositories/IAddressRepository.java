package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAddressRepository extends JpaRepository<Address, Long> {
    Address findByHouseNumberAndZipcodeAndApartmentNumber(int houseNumber, String zipcode, String apartmentNumber);
}
