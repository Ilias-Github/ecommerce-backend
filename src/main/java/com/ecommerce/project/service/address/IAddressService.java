package com.ecommerce.project.service.address;

import com.ecommerce.project.payload.AddressDTO;
import org.springframework.stereotype.Service;

@Service
public interface IAddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    AddressDTO deleteAddress(Long addressId);
}
